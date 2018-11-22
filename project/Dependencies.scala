import sbt._

object Dependencies {
  lazy val fs2 = Seq(
    "co.fs2" %% "fs2-core",
    "co.fs2" %% "fs2-io",
    "co.fs2" %% "fs2-reactive-streams",
    "co.fs2" %% "fs2-experimental"
  ).map(_ % "1.0.0")
  lazy val circe = Seq(
    "io.circe" %% "circe-core",
    "io.circe" %% "circe-generic",
    "io.circe" %% "circe-parser",
    "io.circe" %% "circe-fs2",
    "io.circe" %% "circe-java8"
  ).map(_ % "0.10.0")

  lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.0.5" % Test
  lazy val scalaCheck = "org.scalacheck" %% "scalacheck" % "1.14.0" % Test
  lazy val cucumber = Seq(
    "io.cucumber" % "cucumber-core",
    "io.cucumber" %% "cucumber-scala",
    "io.cucumber" % "cucumber-jvm",
    "io.cucumber" % "cucumber-junit",
  ).map(_ % "2.0.1" % Test)
  lazy val javaCompatibility = "org.scala-lang.modules" %% "scala-java8-compat" % "0.9.0" % Test
  lazy val dockerTest = Seq(
    "com.whisk" %% "docker-testkit-scalatest",
    "com.whisk" %% "docker-testkit-impl-docker-java",
  ).map(_ % "0.9.8" % Test)
}
