package util

/**
 * @author alexandregenon
 */
// Some utility functions to manipulate words
package object Word extends Serializable {
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