package service

import core.DefaultTimeout

import scala.util.Try
import akka.actor.ActorSystem
import akka.actor.{ Actor, ActorLogging, ActorRef }
import akka.event.LoggingReceive
import akka.http.scaladsl.model.ws.{ TextMessage, Message }

object LiveLogger {
  case class StrMes(s: String)
  case class Register(a: ActorRef)
  case class Leave(a: ActorRef)
  case object Leave
}

class LiveLoggerActor extends Actor with ActorLogging with DefaultTimeout {
  import LiveLogger._
  import MPLSolver.SolStats
  var subscriberRegistry: List[ActorRef] = List()

  def logDispatch (message:String) = {
    log.info(message)
    subscriberRegistry.foreach(l => l ! TextMessage(message))
  }
  
  def receive: Receive = LoggingReceive {
    case StrMes(s)   => logDispatch(s)
    case s: String   => logDispatch(s)
    case s: SolStats => logDispatch(s.toString)
    case Register(a) => {
      subscriberRegistry = a :: subscriberRegistry
      log.info(s"$a is registered, we now have ${subscriberRegistry.size} listeners")
    }
    case Leave(a) => subscriberRegistry = subscriberRegistry.filterNot { _ == a }
    case m:Message => log.info(s"received message $m")
    case m: Any   => log.error(s"Unknown message of type ${m.getClass} received ")
  }

}