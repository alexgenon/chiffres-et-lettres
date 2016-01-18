package service
import akka.actor.{ Actor, ActorLogging }
import scala.util.Try
import scala.annotation.tailrec
import java.io._
import core.SparkConfig._
import org.apache.spark.rdd.RDD
import util.MultiSet

/**
 * @author alexandregenon
 * log every request into a repository (currently a dumb file)
 * Detached from the solver to make the 2 activities (logging and solving) separated and concurrent
 */


trait MotPlusLongLogger {
  def stats:Map[String,String] = {
    Map("coucou" ->"gamin")
  }
}

class MotPlusLongLoggerActor extends Actor with ActorLogging with MotPlusLongLogger{
  import MPLSolver._
  val logFile = new FileWriter("challenges.log",true)
  
  def receive: Receive = {
    case m:SolStats => {
      log.info(s"""Received ${m.toString} to log""")
      logFile.write(m.toCSV)
      logFile.flush()
    }
    
    case Stats => {
      log.info("Received stats request")
      sender ! stats
    }
    
    case _ => {
      log.error("Unknown message received")
    }
  }
  
  override def postStop ={
    logFile.close
  }
}