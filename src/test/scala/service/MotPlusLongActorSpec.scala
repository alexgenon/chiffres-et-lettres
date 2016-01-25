package service


import org.apache.log4j.{Logger, Level}
import org.specs2.mutable._
import org.specs2.specification.core.{Fragments}
import org.specs2.specification.{Scope, Step, AroundEach}
import org.specs2.execute.{Result, AsResult}
import scala.util.{Success, Failure, Try}
import core.SparkConfig._

/**
 * @author alexandregenon
 */
class MotPlusLongActorSpec extends Specification with MPLSolver{
 sequential
 
 implicit val dictFilename = "src/test/resources/test_dict.txt"
 import scala.concurrent.ExecutionContext.Implicits.global
 
 reindex
 val noluck = "zrtohzp"
 s"No word for $noluck" >> {
   solve(noluck)._1.isEmpty must_== true
 }
 
 val shouldwork = "gpanrede".toLowerCase
 s"Expects 2 results for $shouldwork" >> {
   val solutions = solve(shouldwork)
   val solutionsGrouped = solutions._1.groupBy(_.length)
   println(s"""Solutions for $shouldwork""")
   solutionsGrouped.foreach {case (n,l) => println(s"""$n : ${l.mkString(",")}""")}
   solutions._1.size must_== 2
 }
}

trait MPLSearchContext extends AroundEach {
  private def destroyContext() {
    sparkContext.stop()
  }
}