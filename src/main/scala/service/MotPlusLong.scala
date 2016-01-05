package service
import akka.actor.{ Actor, ActorLogging }
import scala.util.Try
import core.SparkConfig._


/**
 * @author alexandregenon
 */
object MPLSolver {
  case class Input(l:List[String])
  case class ReIndex()
}
trait MPLSolver{
  
  
}

class MotPlusLong extends Actor with ActorLogging with MPLSolver{
  import MPLSolver._
  def receive:Receive = {
    case Input(l) => {
      log.info("""Received ${l.mkstring(",")} as input""")
    }
    case ReIndex() => {
      log.info("""Received request for reindexing""")
    }
  } 
}