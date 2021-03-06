package service

import akka.actor.Actor
import akka.event.LoggingReceive
import scala.util.{Try,Success,Failure}
import util.MultiSet
/**
 * @author alexandregenon
 */

object CEBSolver {
  case class Input(goal: Int,numbers: List[Int])
  abstract class ArithmeticOperation {
    def compute(left: Int, right: Int): Option[Int]
    def apply(left: Int, right: Int): Option[Int] = compute(left, right)
    def isCommutative: Boolean
  }

  object Add extends ArithmeticOperation {
    def compute(left: Int, right: Int): Option[Int] = Some(left + right)
    override def toString = "+"
    def isCommutative = true
  }
  object Sub extends ArithmeticOperation {
    def compute(left: Int, right: Int): Option[Int] = if (left >= right) Some(left - right) else None
    override def toString = "-"
    def isCommutative = false
  }
  object Mul extends ArithmeticOperation {
    def compute(left: Int, right: Int): Option[Int] = Some(left * right)
    override def toString = "*"
    def isCommutative = true
  }
  object Div extends ArithmeticOperation {
    def compute(left: Int, right: Int): Option[Int] = if (right != 0 && left % right == 0) Some(left / right) else None
    override def toString = "/"
    def isCommutative = false
  }


  case class Step(op: ArithmeticOperation, left: Int, right: Int) {
    override def toString = left + " " + op + " " + right + " = " + op.compute(left, right).getOrElse("Didju :-/")
    def compute: Option[Int] = op.compute(left, right)
  }
  object Step {
    def getValidStep(o: ArithmeticOperation, o1: Int, o2: Int): Option[Step] =
      // Note : to simplify this logic, we rely on the fact that o.isCommutative => always o.compute.isDefined
      // and not commutative implies that only 1 possible combination is defined 
      if (o.isCommutative)
        Some(Step(o, o1, o2))
      else if (o.compute(o1, o2).isDefined)
        Some(Step(o, o1, o2))
      else if (o.compute(o2, o1).isDefined)
        Some(Step(o, o2, o1))
      else None
  }

  case class Numbers(numbers: MultiSet[Int]) {
    def remove(n: Int, m: Int) = Numbers(numbers.remove(n).remove(m))
    def ::(number: Int) = Numbers(number :: numbers)
    def pairs = numbers.getPairs
    def contains(i: Int) = numbers contains (i)
    override def toString = "(" + numbers.mkString(",") + ")"
  }

  class Path(operations: List[Step], val finalNumbers: Numbers, val previousPath: Option[Path] = None) {
    def extend(move: Step, updatedNumbers: Numbers) = new Path(move :: operations, updatedNumbers, Some(this))
    def getSteps = operations reverse
    def lastOperation = if (operations.nonEmpty) Some(operations.head) else None
    override def toString = getSteps mkString ("\n")
  }

}

trait CEBSolver{
  import CEBSolver._
  val operations = List[ArithmeticOperation](Add, Sub, Mul, Div)

  class Solver(initialNumbers: List[Int], goal: Int) {
    def from(paths: Set[Path]): Stream[Set[Path]] = {
      if (paths.isEmpty) Stream.empty
      else {
        val more = for {
          path <- paths
          (l, r) <- path.finalNumbers.pairs
          op <- operations
          s <- Step.getValidStep(op, l, r)
        } yield path extend (
          s,
          (s.compute).get :: (path.finalNumbers remove (l, r)))
        paths #:: from(more)
      }
    }

    val initialPath = new Path(Nil, Numbers(MultiSet(initialNumbers)))
    val pathSets = from(Set(initialPath))

    def solve: Stream[Path] = {
      for {
        pathSet <- pathSets
        path <- pathSet
        if path.finalNumbers contains goal
      } yield path
    }
  }
}

class CompteEstBonActor extends Actor with CEBSolver {

  import CEBSolver._
  val liveLoggerActor = core.Boot.system.actorSelection("/user/gds/livelogger")
  
  def receive: Receive = LoggingReceive {
    case Input(goal,numbers) => {
      val solution = Try{(new Solver(numbers,goal)).solve.head}
      sender ! solution
      
      val logMess =s"""
        Received challenge for $goal with numbers ${numbers.mkString(",")}
        Solution is ${solution.toString}
      """
      liveLoggerActor ! logMess
    }
  }
}