package util
import scala.annotation.tailrec

/**
 * @author alexandregenon
 */
case class MultiSet[T: Ordering](val map: Map[T, Int]) extends java.io.Serializable {
  def contains(n: T): Boolean = map contains n
  def containsAll(l: List[T]) = {
    @tailrec
    def test(l: List[T], current: MultiSet[T]): Boolean = l match {
      case head :: tail => if (current.contains(head)) test(tail, current.remove(head))
      else false
      case List() => true
    }
    test(l, this)
  }
  def get(n: T): Int = map getOrElse (n, 0)
  def remove(n: T): MultiSet[T] = {
    val e = map get n
    e match {
      case None                         => MultiSet(map)
      case e: Some[Int] if (e.get == 1) => MultiSet(map - n)
      case e: Some[Int] if (e.get > 0)  => MultiSet(map updated (n, e.get - 1))
    }
  }
  def add(n: T): MultiSet[T] = MultiSet(map updated (n, map.getOrElse(n, 0) + 1))
  def ::(n: T) = add(n)
  def getPairs: List[(T, T)] = {
    @tailrec
    def rec(l: List[T], acc: List[(T, T)]): List[(T, T)] = l match {
      case List() => acc
      case head :: tail => {
        val subList = tail.map((head, _))
        if (map.get(head).get >= 2)
          /* The reason why we can't use the Scala .combinations function
               * over list without a trick such as 
               * map.flatMap(x => {if (x._2>=2) List(x._1,x._1) else List(x._1)}).toList */
          rec(tail, ((head, head) :: subList) ++ acc)
        else
          rec(tail, subList ++ acc)
      }
    }
    rec(map.keys.toList.sorted, List())
  }
  def combinations(n: Int): Iterator[List[T]] = map.keys.toList.sorted.combinations(n)
  def mkString(sep: String): String = map.flatMap(x => List.fill(x._2)(x._1)).mkString(sep)
}

object MultiSet extends java.io.Serializable {
  def apply[T: Ordering](l: List[T]): MultiSet[T] = l match {
    case List()       => MultiSet(Map[T, Int]())
    case head :: tail => MultiSet(tail).add(head)
  }
}
