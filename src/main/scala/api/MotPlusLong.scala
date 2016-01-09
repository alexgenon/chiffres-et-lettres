package api
import core.DefaultTimeout
import akka.actor.ActorSystem
import spray.routing.Directives
import spray.httpx.TwirlSupport
import spray.json._
import akka.pattern.ask
import scala.util.matching.Regex
import scala.util.Try
import service.MPLSolver._

/**
 * @author alexandregenon
 */
object MPLJSonProtocol extends Marshalling {
  
  implicit object Solutions extends RootJsonFormat[List[String]]{
    def write(sols:List[String]) = JsArray(sols.map(JsString(_)).toVector)
    def read(v:JsValue) = ??? 
  }
}

class MotPlusLongApi (implicit val actorSystem : ActorSystem) extends Directives with DefaultTimeout with TwirlSupport {
  import scala.concurrent.ExecutionContext.Implicits.global
  import MPLJSonProtocol._
  val solverActor = actorSystem.actorSelection("/user/gds/mplsolver")
  
  val reindex = path("reindex") {
    get {
      solverActor ! ReIndex
      complete("reindexing in progress!")
    }
  }
  
  val solve = path("solve"/ """[a-zA-Z]+""".r) { candidates =>
    get { 
      complete {
        (solverActor ? Input(candidates)).mapTo[List[String]]
      }
    }
  }
  
  val routes = pathPrefix("mpl") {
    reindex ~ solve
  }
}
