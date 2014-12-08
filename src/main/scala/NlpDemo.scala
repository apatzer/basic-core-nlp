/**
 * Simple demo to train a classifier using a tab-delimited training set given in a text file as arg(0).
 *
 * Uses OpenNLP & Sista NLP.  Not meant for production use by any means!
 */
object NlpDemo {
  def main(args: Array[String]) {
    val file = args(0)
    val query = "Recipes for tomato soup"
    val textClassifier = new TextClassifier(file)
    val (classification, probability) = textClassifier.classify(query)
    System.out.println(s"Q: $query\nClassification: $classification\nProbability: %01.1f%%".format(100 * probability))
  }
}
