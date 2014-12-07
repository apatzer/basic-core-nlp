basic-open-nlp
==============

Classify text using Stanford's Open NLP with Sista NLP Scala wrappers

This basic system takes in a text file which each line in tab-delimited format {class document_text}:
training.txt
```
...
Cooking  How do a make a good tomato sauce for pizza?
Gardening  My are my cherry tomatoes dying?
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