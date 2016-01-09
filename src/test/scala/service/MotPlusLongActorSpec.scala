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
 
 import scala.concurrent.ExecutionContext.Implicits.global
 
 reindex
 val noluck = "zrtohzp"
 s"No word for $noluck" >> {
   solve(noluck).isEmpty must_== true
 }
 
 val shouldwork = "AOPRIUTNR".toLowerCase
 s"Expects 136 results for $shouldwork" >> {
   val solutions = solve(shouldwork)
   val solutionsGrouped = solutions.groupBy(_.length)
   println(s"""Solutions for $shouldwork""")
   solutionsGrouped.foreach {case (n,l) => println(s"""$n : ${l.mkString(",")}""")}
   solutions.size must_== 136
 }
}

trait MPLSearchContext extends AroundEach {
  private def destroyContext() {
    sparkContext.stop()
  }
}