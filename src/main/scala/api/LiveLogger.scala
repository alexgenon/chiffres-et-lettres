package api

import core.DefaultTimeout
import service.LiveLogger._

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer,OverflowStrategy}
import akka.stream.scaladsl.{Flow,Source,Sink}
import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.model.ws.{TextMessage,Message}


class LiveLoggerApi (implicit val actorSystem: ActorSystem)  extends Directives with DefaultTimeout {
  
  import scala.concurrent.ExecutionContext.Implicits.global
  import scala.concurrent.duration._
  import scala.concurrent.Await
  
  implicit val actorMaterializer = ActorMaterializer();
  
  val liveLoggerActor = actorSystem.actorSelection("/user/gds/livelogger")
  
  def createFlow = {
    val subscriberFlow = Flow.fromSinkAndSourceMat(
        Sink.foreach[Message] { m => liveLoggerActor ! m },
        Source.actorRef[Message](10,OverflowStrategy.dropHead).merge (
            Source.tick(FiniteDuration(0,"s"),FiniteDuration(10,"s"),TextMessage("ping")))
        )
        {case (m1,m2) => m2 }
        .mapMaterializedValue {
          case actor => {
            liveLoggerActor ! Register(actor)
            actor ! TextMessage("Successfully registered")
            actor
          }
       }
    
    subscriberFlow
  }
  
  val routes = pathPrefix("logs") {
    path("live"){
      get {
        handleWebsocketMessages(createFlow)
      }
    }
  }
}