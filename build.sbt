resolvers += "Sun Maven2 Repo" at "http://download.java.net/maven/2"
resolvers += "Oracle Maven2 Repo" at "http://download.oracle.com/maven"

lazy val nlp = (project in file("."))
	.settings(
      name := "nlp",
      libraryDependencies ++= Seq("edu.stanford.nlp" % "stanford-corenlp" % "3.6.0" classifier "models",
                                  "edu.arizona.sista" % "processors" % "3.3")
    )