import java.util.Calendar

import edu.arizona.sista.processors.corenlp.CoreNLPProcessor
import edu.stanford.nlp.classify.{LinearClassifier, Dataset, LinearClassifierFactory}
import edu.stanford.nlp.ling.BasicDatum

import scala.collection.JavaConverters._
import scala.io.Source
import scala.util.Random

/**
 * Build a classifier from a tab-delimited file.
 *
 * ...
 * Cooking  How do I make a good tomato sauce for pizza?
 * Gardening  Why are my cherry tomatoes dying?
 * ...
 *
 * Created by apatzer on 12/7/14.
 */
class TextClassifier(trainingFile: String) {

  type Classification = String
  protected lazy val textClassifier: LinearClassifier[Classification, String] = build(trainingFile)
  protected lazy val proc = new CoreNLPProcessor()

  def build(file: String): LinearClassifier[Classification, String] = {

    // Read in gold-set and annotate each example
    var goldSet = Source.fromFile(file).getLines().map(_.split("\t")).map { line =>
      val (tag, content) = (line(0), line(1))
      val datum = createDatum(content)
      datum.setLabel(tag)
      (tag, content, datum)
    }.toList

    // Split into training (80%) vs. test (20%) sets
    Random.setSeed(123)
    goldSet = Random.shuffle(goldSet)
    val split = (goldSet.size * 0.80).toInt
    val training = new Dataset[Classification, String](split)
    goldSet.take(split).foreach(d => training.add(d._3))
    val test = goldSet.drop(split)
    System.out.println(s"Total features: ${training.numFeatures()}")

    // Train a classifier.  Convergence tolerance = 1e-4.  Sigma smoothing = 1.0 (decrease if system is over-trained)
    val lcf        = new LinearClassifierFactory[Classification, String](1e-4, false, 1.0)
    val classifier = lcf.trainClassifier(training)

    val bestFeatures = classifier.toBiggestWeightFeaturesString(false, 50, true)
    System.out.println("Top 50 overall features:\n" + bestFeatures + "\n")

    // Test the system
    val correct = test.count { case (tag, content, datum) =>
      val scores = classifier.probabilityOf(datum).entrySet().asScala
                             .map(e => e.getKey -> e.getValue).toList.sortBy(_._2).reverse
      scores.head._1 == tag
    }
    System.out.println("Accuracy: %01.1f%%".format(100.0 * correct / test.size))

    classifier
  }


  /** Turn this String into a feature vector */
  def createDatum(s: String): BasicDatum[Classification, String] = {
    val doc = proc.mkDocument(s)
    proc.tagPartsOfSpeech(doc)        // Adjective, noun, verb?  See Penn Treebank tags: http://www.ling.upenn.edu/courses/Fall_2007/ling001/penn_treebank_pos.html
    proc.lemmatize(doc)               // Smart stemming of words:  tomato == tomatoes, am == is == are, have == had

    new BasicDatum[Classification, String](FeatureFactory.createFeatures(doc).asJavaCollection)
  }


  /** Classify a string of unknown class and get back probabilities for each class */
  def classify(s: String): (Classification, Double) = {
    val counter = textClassifier.probabilityOf(createDatum(s))
    val scores  = counter.entrySet().asScala.map(e => e.getKey -> e.getValue).toList.sortBy(_._2).reverse
    (scores.head._1, scores.head._2.toDouble)
  }
}
