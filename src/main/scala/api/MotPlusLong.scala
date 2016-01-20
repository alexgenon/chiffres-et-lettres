package api

import core.DefaultTimeout
import service.MPLSolver._

import scala.util.matching.Regex
import scala.util.Try
import akka.actor.ActorSystem
import akka.pattern.ask
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json._



/**
 * @author alexandregenon
 */
object MPLJSonProtocol extends SprayJsonSupport with DefaultJsonProtocol  {
  implicit object Solutions  {
    def write(sols: List[String]) = JsArray(sols.map(JsString(_)).toVector)
    def read(v: JsValue) = ???
  }
}

class MotPlusLongApi(implicit val actorSystem: ActorSystem) extends Directives with DefaultTimeout with TwirlSupport {
  import scala.concurrent.ExecutionContext.Implicits.global
  import MPLJSonProtocol._
  val solverActor = actorSystem.actorSelection("/user/gds/mplsolver")

  val reindex = path("reindex") {
    get {
      complete {
        solverActor ! ReIndex
        "reindexing in progress!"
      }
    }
  }

  val stats = pathPrefix("stats") {
    pathEnd {
      get {
        complete {
          (solverActor ? Stats).mapTo[Map[String, String]]  
        }
      }
    } ~
    path("summary") {
      get {
        complete {
          (solverActor ? Stats).mapTo[Map[String, String]]
        }
      }
    } ~
    path("ui") {
      get {
        complete {
         html.mpl_stats()
        }
      }
    }
  }

  val solve = path("solve" / """[a-zA-Z]+""".r) { candidates =>
    get {
      complete {
        (solverActor ? Input(candidates)).mapTo[List[String]]
      }
    }
  }
  
  val dictionnary = pathPrefix("dictionnary") {
    path("[a-zA-Z]+".r) { word =>
      post{
        complete {
          solverActor ! NewWord(word)
          "Word added successfully"
        }
      }
    } ~
    pathEnd {
      get {
        complete {
          "ok"
        }
      }
    }
  }

  val routes = pathPrefix("mpl") {
    reindex ~ solve  ~ stats ~ dictionnary
  }
}
