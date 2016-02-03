package service
import core.DefaultTimeout
import core.SparkConfig._
import util.MultiSet
import util.Word
import akka.actor.{ Actor, ActorLogging, ActorSystem }
import akka.pattern.{ask,pipe}
import akka.event.LoggingReceive
import scala.util.{Try,Success}
import scala.annotation.tailrec
import org.apache.spark.rdd.RDD
import java.io.FileWriter

/**
 * @author alexandregenon
 */
object MPLSolver {
  case class Input(l: String)
  case class SolStats(challenge:String,nbBuckets:Long,nbCandidates:Long,nbSolutions: Int){
    override def toString = s"$challenge received, had $nbBuckets buckets with a total of $nbCandidates candidates $nbSolutions are solutions"
    def toCSV = s"$challenge;$nbBuckets;$nbCandidates;$nbSolutions\n"
  }
  case object ReIndex
  case object DetailedStats
  case object Stats
  case class NewWord(w:String)
  
  val KEYSIZE = 5
  val WORDLIMIT = 10
  var dictGrouped: RDD[(List[Char], scala.collection.immutable.Set[String])] = null
  var dict: RDD[String] = null
}

trait MPLSolver{
  import MPLSolver._
  implicit val dictFilename:String
  
  def reindex = {
    dict = sparkContext.textFile(dictFilename).filter(x => x.length >= KEYSIZE && x.length <= WORDLIMIT)
    val dict5 = dict.flatMap(w => Word.toMultiSet(w).combinations(KEYSIZE).map(t => (t, w)))
    dictGrouped = dict5.aggregateByKey(Set[String]())((s, w) => s + w, (s1, s2) => s1 ++ s2)
    dictGrouped.persist
  }
  
  def solve(c: String): (List[String],SolStats) = {
    val cSet = MultiSet(Word.normalize(c).toList)
    val cTuples = cSet.combinations(KEYSIZE).toSet
    val candidates = dictGrouped.filter(x => cTuples.contains(x._1))
    val nbBuckets = candidates.count
    val wordCandidates = candidates.flatMap(_._2)
    val nbCandidates = wordCandidates.count
    val solutions = wordCandidates.filter(w => Word.canBeEncoded(w, cSet)).collect
      .toList.sorted(Ordering[Int].on[String](_.length).reverse).distinct
    (solutions,SolStats(c,nbBuckets,nbCandidates,solutions.size))
  }
  
  def stats: Map[String, String] = {
    def choose(n1: Int, n2: Int): Int = {
      @tailrec
      def mult(n1: Int, n2: Int, acc: Int = 1): Int = {
        if (n1 <= n2) acc * n2
        else mult(n1 - 1, n2, acc * n1)
      }
      val max = scala.math.max(n2, n1 - n2)
      val min = n1 - max
      mult(n1,max+1) / mult(min,1)
    }
    val sizes = dictGrouped.map(t => (t._1, t._2.size))
    val maxB = sizes.max()(Ordering[Int].on[((List[Char]), Int)](_._2))
    val avgB = sizes.aggregate(0)((s, t) => s + t._2, (s1, s2) => s1 + s2).toDouble / sizes.count
    Map("dictionnary_size" -> dict.count.toString,
      "bucket_key_size" -> KEYSIZE.toString,
      "bucket_key_combinations" -> choose(26,KEYSIZE).toString,
      "buckets_count" -> sizes.count.toString,
      "buckets_max" -> maxB.toString,
      "buckets_average" -> avgB.toString)
  }
}

class MotPlusLongActor extends Actor with ActorLogging with DefaultTimeout with MPLSolver {
  import MPLSolver._
  
  import scala.concurrent.ExecutionContext.Implicits.global
  implicit val dictFilename = "src/main/resources/liste_francais.txt"
  val loggerActor = core.Boot.system.actorSelection("/user/gds/mpllogger")
  
  override def preStart = {
    reindex
  }
  def receive: Receive = LoggingReceive {
    case ReIndex => {
      log.info("Received request for reindexing")
      reindex
    }
    case Input(c) => {
      log.info(s"""Received $c as input""")
      val (solution,stats) = solve(c)
      loggerActor ! stats
      sender ! solution
    }
    case Stats => {
      log.info("Received stats request")
      import scala.concurrent.Future
      val theirStats = (loggerActor ? Stats).mapTo[Map[String,String]]
      val ourStats = stats
      val loggedStats = theirStats map {
        case s => ourStats ++ s
      }
      loggedStats pipeTo (sender)
    }
    case NewWord(w) => {
      log.info(s"Request to add $w to dictionnary")
      val dictFile = new FileWriter(dictFilename,true)
      dictFile.write("\n"+w)
      dictFile.close
    }
    case _ => {
      log.error("Unknown message received")
    }
  }
}