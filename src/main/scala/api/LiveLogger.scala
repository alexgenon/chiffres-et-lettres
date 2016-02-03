package api

import core.DefaultTimeout
import service.LiveLogger._

import akka.actor.ActorSystem
import akka.stream.{ ActorMaterializer, OverflowStrategy }
import akka.stream.scaladsl.{ Flow, Source, Sink }
import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.model.ws.{ TextMessage, Message }

/* 
 * Handle each WebSocket connection using the Flow defined below.
 * Was quite challenging as Flow fits well to a request-response call flow but here we aimed for a 
 * server-push call flow (see description of subscriberFlow below).
 * The actor handl
 */
class LiveLoggerApi(implicit val actorSystem: ActorSystem) extends Directives with DefaultTimeout {

  import scala.concurrent.ExecutionContext.Implicits.global
  import scala.concurrent.duration._
  import scala.concurrent.Await

  implicit val actorMaterializer = ActorMaterializer();

  /* The actor handling all messages */
  val liveLoggerActor = actorSystem.actorSelection("/user/gds/livelogger")

  /* This Flow is defined by a Source and a Sink 
   * The Source is a source based on an actorRef to which liveLoggerActor will send the log message
   * merged with a Source.tick sending "ping" every 10s for websocket keep alive
   * 
   * The Sink simply transmit each message to the liveLoggerActor
   * 
   * For the Source based on an actorRef to work, we have to call mapMaterialized value to make 
   */
  lazy val subscriberFlow = Flow.fromSinkAndSourceMat(
    Sink.foreach[Message] { m => liveLoggerActor ! m },
    Source.actorRef[Message](10, OverflowStrategy.dropHead).merge(
      Source.tick(FiniteDuration(0, "s"), FiniteDuration(10, "s"), TextMessage("ping")))) { case (m1, m2) => m2 }
    .mapMaterializedValue {
      case actor => {
        liveLoggerActor ! Register(actor)
        actor ! TextMessage("Successfully registered")
        actor
      }
    }

  val routes = pathPrefix("logs") {
    path("live") {
      get {
        handleWebsocketMessages(subscriberFlow)
      }
    }
  }
}