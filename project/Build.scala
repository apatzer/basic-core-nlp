import sbt.Keys._
import sbt._

object Resolvers {
  val sunrepo    = "Sun Maven2 Repo" at "http://download.java.net/maven/2"
  val oraclerepo = "Oracle Maven2 Repo" at "http://download.oracle.com/maven"

  val resolvers = Seq (sunrepo, oraclerepo)
}

object Build extends sbt.Build {
  lazy val nlp = (project in file(".")).
    settings(
      name := "nlp",
      resolvers ++= Resolvers.resolvers,
      libraryDependencies ++= Seq("edu.stanford.nlp" % "stanford-corenlp" % "3.6.0" classifier "models",
                                  "edu.arizona.sista" % "processors" % "3.3")
    )
}