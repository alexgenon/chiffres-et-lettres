package service

import org.apache.log4j.{Logger, Level}
import org.specs2.mutable._
import org.specs2.specification.core.{Fragments}
import org.specs2.specification.{Scope, Step, AroundEach}
import org.specs2.execute.{Result, AsResult}
import scala.util.{Success, Failure, Try}

/**
 * @author alexandregenon
 */
class CompteEstBonActorSpec extends Specification with CEBSolver {
  sequential
  
  import scala.concurrent.ExecutionContext.Implicits.global
  
  "Find 3 solutions to (10,3,5,4) with goal 125" >> { 
    val results = new Solver(List(10,3,5,4),125).solve.toList
    results.foreach(println)
    results.size must_== 3
  }
  
  "Find no solution to (10,3,5,4) with goal 999" >> {
    val results = new Solver(List(10,3,5,4),999).solve.toList
    results.size must_== 0
  }
}