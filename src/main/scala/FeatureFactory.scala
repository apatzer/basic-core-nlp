import edu.arizona.sista.processors.{Sentence, Document}

import scala.collection.mutable

/**
 * Features in NLP the patterns or rules governing how a vector representation of a document is created.
 *  - Word:  Look at the frequency with which a word is associated to a classification
 *  - Word pairs
 *
 * Created by apatzer on 12/7/14.
 */
trait FeatureCreator {
  def hasSentenceFeatures: Boolean = true
  def createFeatures(sentence: Sentence): Iterable[String]
}

object FeatureFactory {
  protected val creators = List(WordFeature, LowerCaseLemmaFeature, WordPairFeature)

  def createFeatures(doc: Document): Set[String] = {
    doc.sentences.toList.flatten { sentence =>
      creators.filter(_.hasSentenceFeatures).flatMap(_.createFeatures(sentence))
    }.toSet
  }
}

object Util {
  /* Classic short word stop words list */
  val stopWords = Set("a", "an", "and", "are", "as", "at", "be", "by", "did", "for", "from", "has", "he",
    "i", "in", "is", "it", "its", "of", "on", "that", "the", "to", "was", "were", "will", "with",
    "hi", "hello", "question", "answer", "?", "'s"
  )

  /* Filter out stop words */
  def filterStopWords(l: Iterable[String]): Iterable[String] = l.filterNot(s => stopWords.contains(s.toLowerCase))
}

object WordFeature extends FeatureCreator {
  def createFeatures(sentence: Sentence) = {
    Util.filterStopWords(sentence.words)
      .map("S-" + _)
  }
}

/**
 * Create a feature for each case-insensitive lemma word.
 * Exclude stop words and numbers.
 */
object LowerCaseLemmaFeature extends FeatureCreator {
  def createFeatures(sentence: Sentence) = {
    sentence.lemmas.map { lemmas =>
      Util.filterStopWords(lemmas)
        .map("S-" + _.toLowerCase)
    }.getOrElse(Nil)
  }
}

/**
 * Create a "SWP-prev-next" feature from all adjacent non-stop-word lemma pairs that are nouns, adjectives, and verbs.
 */
object WordPairFeature extends FeatureCreator {
  def createFeatures(sentence: Sentence) = {
    val r = new mutable.HashSet[String]()
    (sentence.lemmas, sentence.tags) match {
      case (Some(lemmas), Some(tags)) =>
        if (lemmas.size > 1) {
          val zip = tags.zip(lemmas).toList
          zip.drop(1).foldLeft(zip.head) { case ((firstTag, firstLemma), (secondTag, secondLemma)) =>
            if (matchesPos(firstTag) && matchesPos(secondTag)) {
              r += s"SWP-$firstLemma-$secondLemma"
            }
            (secondTag, secondLemma)
          }
        }
      case _ => // Nothing
    }
    r
  }

  /**
   * Only create word pairs for combinations of nouns, verbs, and adjectives:
   * "white house" JJ-NN
   * "Paris France" NNP-NNP
   *
   * Without this restriction, word pairs starts to get very large with meaningless combinations of small words,
   * punctuation, symbols, etc.
   */
  def matchesPos(tag: String): Boolean = {
    tag match {
      case "NN" | "NNS" | "NNP" | "NNPS" | "JJ" | "VB" | "VBP" | "VBD" | "VBG" | "VBN" | "VBZ" => true
      case _ => false
    }
  }
}

/**
 * Count non-alpha / whitespace / control characters.
 */
object NonAlphaCountFeature extends FeatureCreator {
  override def hasSentenceFeatures: Boolean = false

  def createFeatures(sentence: Sentence) = {
    val r = new mutable.HashMap[Char, Int]
    sentence.words.foreach { word =>
      word.foreach { c =>
        if (!(c.isLetter || c.isWhitespace || c.isControl)) {
          if (c.isDigit)
            r.put('d', r.getOrElse('d', 0) + 1)  // Compress all digits into one count
          else
            r.put(c, r.getOrElse(c, 0) + 1)
        }
      }
    }

    // Bucket counts to increase chances of matching something else later
    r.map { case (c, count) =>
      val s = count match {
        case 1 =>         "CHC-" + c
        case 2 | 3 =>     "CHC-" + c + "-2+"
        case 4 | 5 | 6 => "CHC-" + c + "-4+"
        case _ =>         "CHC-" + c + "-7+"
      }
      s
    }
  }
}
