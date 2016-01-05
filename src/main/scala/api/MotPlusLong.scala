package api
import core.DefaultTimeout
import akka.actor.ActorSystem
import spray.routing.Directives
import spray.httpx.TwirlSupport
import spray.json._
import akka.pattern.ask
import scala.util.matching.Regex
import scala.util.Try

/**
 * @author alexandregenon
 */
class MotPlusLongApi (implicit val actorSystem : ActorSystem) extends Directives with DefaultTimeout with TwirlSupport {
  import scala.concurrent.ExecutionContext.Implicits.global
  
  val reindex = path("reindex") {
    get {
      complete("reindexing FTW !")
    }
  }
  
  val solve = path("solve"/ """[a-zA-Z]+""".r) { candidates =>
    get { 
      complete(s"Find a word with $candidates")
    }
  }
  
  val routes = pathPrefix("mpl") {
    reindex ~ solve
  }
}
