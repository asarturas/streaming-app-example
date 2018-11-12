import Dependencies._

lazy val root = (project in file(".")).
  enablePlugins(CucumberPlugin).
  settings(
    inThisBuild(List(
      organization := "com.spikerlabs",
      scalaVersion := "2.12.7",
      version      := "0.1.0-SNAPSHOT"
    )),
    name := "streaming-app-example",
    libraryDependencies ++= fs2,
    libraryDependencies ++= circe
  )
  // test dependencies
  .settings(
    libraryDependencies += scalaTest,
    libraryDependencies += scalaCheck,
    libraryDependencies ++= cucumber,
    CucumberPlugin.monochrome := false,
    CucumberPlugin.glue := "classpath:steps",
    CucumberPlugin.features := List("classpath:features")
  )