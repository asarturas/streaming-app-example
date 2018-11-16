package com.spikerlabs.streamingapp

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}

import org.scalatest.{AppendedClues, BeforeAndAfterAll, FlatSpec, Matchers}

class AppSpec extends FlatSpec with Matchers with AppendedClues {

  it should "throw an exception when not enough attributes are passed to the app" in {
    an[Exception] shouldBe thrownBy(App.run(Nil).attempt.unsafeRunSync())
  }

  it should "read data from a specified file and write results to a specified file" in {
    val inputFilePath =  Thread.currentThread().getContextClassLoader(). getResource("examples/simple.data").getPath
    val outputFilePath = "/tmp/streamingapp-example-result-app-spec.data"

    App.run(List(inputFilePath, outputFilePath)).attempt.unsafeRunSync()

    val processingResult = new String(
      Files.readAllBytes(Paths.get(outputFilePath)),
      StandardCharsets.UTF_8.name()
    ).trim
    val expectedResults = new String(
      Files.readAllBytes(Paths.get( Thread.currentThread().getContextClassLoader().getResource("examples/simple-data-expected-result.data").getPath)),
      StandardCharsets.UTF_8.name()
    ).trim

    processingResult shouldBe expectedResults
  }

}
