import Dependencies._

lazy val root = (project in file(".")).
  enablePlugins(CucumberPlugin, JavaAppPackaging, DockerPlugin).
  settings(
    inThisBuild(List(
      organization := "com.spikerlabs",
      scalaVersion := "2.12.7",
      version      := "0.1.0-SNAPSHOT"
    )),
    name := "streaming-app-example",
    mainClass := Some("com.spikerlabs.streamingapp.App"),
    dockerUsername in Docker := Some("spikerlabs"),
    version in Docker := "latest",
    libraryDependencies ++= fs2,
    libraryDependencies ++= circe
  )
  // test dependencies
  .settings(
    libraryDependencies += scalaTest,
    libraryDependencies += scalaCheck,
    libraryDependencies ++= cucumber,
    libraryDependencies += javaCompatibility,
    libraryDependencies ++= dockerTest,
    CucumberPlugin.monochrome := false,
    CucumberPlugin.glue := "classpath:steps",
    CucumberPlugin.features := List("classpath:features")
  )