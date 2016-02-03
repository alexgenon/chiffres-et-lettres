package core

import akka.actor.{Actor, Props}
import service.{CompteEstBonActor,MotPlusLongActor,MotPlusLongLoggerActor,LiveLoggerActor}

case class Startup()
case class Shutdown()

/**
  *  When this actor receives a startup message it creates request handling actors,
  *  when it receives a shutdown message it stops all actors
  */
class ApplicationActor extends Actor {

  def receive: Receive = {
    case Startup() => {
      context.actorOf(Props[CompteEstBonActor],"cebsolver")
      context.actorOf(Props[MotPlusLongActor],"mplsolver")
      context.actorOf(Props[MotPlusLongLoggerActor],"mpllogger")
      context.actorOf(Props[LiveLoggerActor],"livelogger")
      sender ! true
    }
    case Shutdown() => {
      context.children.foreach(actor => context.stop(actor))
    }
  }
}
