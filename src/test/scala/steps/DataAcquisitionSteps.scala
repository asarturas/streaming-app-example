package steps

import java.nio.file.{Files, Path, Paths}

import com.spikerlabs.streamingapp.domain.MessageStream
import cucumber.api.scala.{EN, ScalaDsl}
import cucumber.api.PendingException
import org.scalatest.{AppendedClues, Matchers}

class DataAcquisitionSteps extends ScalaDsl with EN with Matchers with AppendedClues {
  val filePathPrefix = "/tmp/streaming-app-example"
  def filePath(fileName: String): Path = Paths.get(s"$filePathPrefix-$fileName")
  Given("""^there is source file "([^"]*)":$""") { (fileName: String, fileContents: String) =>
    Files.write(filePath(fileName), fileContents.getBytes)
  }
  When("""^I create a message stream from source file "([^"]*)"$""") { fileName: String =>
    sharedState = sharedState.copy(stream = MessageStream.fromFile(filePath(fileName)))
  }
  Then("""^I should get an empty message stream$""") { () =>
    sharedState.stream.compile.toVector.unsafeRunSync() shouldBe empty
  }
}
