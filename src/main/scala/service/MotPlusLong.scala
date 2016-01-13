package service
import akka.actor.{ Actor, ActorLogging }
import scala.util.Try
import core.SparkConfig._
import org.apache.spark.rdd.RDD
import util.MultiSet

/**
 * @author alexandregenon
 */
object MPLSolver {
  case class Input(l: String)
  case class ReIndex()

  // Some utility functions to manipulate words
  object Word extends Serializable {
    import scala.util.matching.Regex
    def normalize(s: String) = {
      s.toLowerCase.replaceAll("[è,é,ê,ë]", "e").replaceAll("[à,á,â,ã,ä]", "a").replaceAll("ç", "c")
        .replaceAll("[ì,í,î,ï]", "i").replaceAll("[ò,ó,ô,õ,ö]", "o").replaceAll("ñ", "n")
        .replaceAll("[ù,ú,û,ü]", "u").replaceAll("[ý,ÿ]", "y").replaceAll("[^a-z]", "")
    }
    def toMultiSet(s: String) = MultiSet(normalize(s).toList)
    def isAlphaPair(t: (Char, Char)): Boolean = (t._1 >= 'a' && t._1 <= 'z' && t._2 >= 'a' && t._2 <= 'z')

    def canBeEncoded(w: String, candidates: MultiSet[Char]): Boolean =
      candidates.containsAll(normalize(w).toList)

  }
  var dictGrouped:RDD[(List[Char], scala.collection.immutable.Set[String])] = null
}
trait MPLSolver {
  import MPLSolver._
  def reindex = {
    val dict = sparkContext.textFile("src/main/resources/liste_francais.txt").filter(x => x.length>=5 && x.length<=10)
    val dict5 = dict.flatMap(w => Word.toMultiSet(w).combinations(5).map(t => (t,w)))
    dictGrouped = dict5.aggregateByKey(Set[String]())((s,w) => s+w,(s1,s2) => s1++s2)
    dictGrouped.persist
  }
  def solve(c:String):List[String] = {
     val cSet = MultiSet(Word.normalize(c).toList)
     val cTuples = cSet.combinations(5).toSet
     val candidates = dictGrouped.filter(x => cTuples.contains(x._1))
     candidates.flatMap(_._2).filter(w => Word.canBeEncoded(w,cSet)).collect
       .toList.sorted(Ordering[Int].on[String](_.length).reverse).distinct
  }
}

class MotPlusLongActor extends Actor with ActorLogging with MPLSolver {
  import MPLSolver._
  override def preStart = {
    reindex
  }
  def receive: Receive = {
    case Input(c) => {
      log.info(s"""Received $c as input""")
      sender ! solve(c) 
    }
    case ReIndex() => {
      log.info("""Received request for reindexing""")
      reindex
    }
  }
}