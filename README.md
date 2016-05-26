basic-open-nlp
==============

Classify text using Stanford's Open NLP with Sista NLP Scala wrappers

This basic system takes in a text file with each line in tab-delimited format {class document_text}:
training.txt
```
...
Cooking  How do I make a good tomato sauce for pizza?
Gardening  Why are my cherry tomatoes dying?
```

Instantiate via
```
val textClassifier = new TextClassifier("training.txt")
```

Unknown text can be classified using:
```
val (classification, score) = textClassifer.classify("I wonder what class these tomatoes will fall into...")
```

That's it.  Be careful.  There is zero input checking and no exception handling.

To run:
```
sbt "run training100.txt"
sbt "run training1000.txt"
```
Be sure to use quotes so SBT recognizes the second word as part of the command (in this case, the file with training data).
