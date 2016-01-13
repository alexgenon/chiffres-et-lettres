package api

import core.DefaultTimeout
import akka.actor.ActorSystem
import spray.routing.Directives
import spray.httpx.TwirlSupport
import spray.json._
import akka.pattern.ask
import scala.util.{ Try}
import service.CEBSolver._

/**
 * @author alexandregenon
 */

object CEBJSonProtocol extends Marshalling {

  implicit object StepFormat extends RootJsonFormat[Step] {
    def write(s: Step) = JsObject(("left", JsNumber(s.left)), ("op", JsString(s.op.toString)), ("right", JsNumber(s.right)))
    def read(v: JsValue) = ??? //don't need it and to lazy to code it 
  }
  implicit object PathFormat extends RootJsonFormat[Path] {
    def write(p: Path) = p.getSteps.toJson
    def read(v: JsValue) = ??? //don't need it and to lazy to code it 
  }
}

class CompteEstBonApi(implicit val actorSystem: ActorSystem) extends Directives with DefaultTimeout with TwirlSupport {
  import scala.concurrent.ExecutionContext.Implicits.global
  import CEBJSonProtocol._
  val solverActor = actorSystem.actorSelection("/user/gds/cebsolver")

  val solve = path("solve" / IntNumber) { goal =>
    parameters('candidates) { (candidates) =>
      get {
        complete {
          (solverActor ? Input(goal, candidates.split(",").toList.map(_.toInt))).mapTo[Try[Path]]
        }
      }
    }
  }

  val routes = pathPrefix("ceb") {
    solve ~ 
    pathEnd {
      get {
        complete{
          html.ceb()
        }
      }
    }
  }
}