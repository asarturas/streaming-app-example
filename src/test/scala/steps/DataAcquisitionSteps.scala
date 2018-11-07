package steps

import java.nio.file.{Files, Path, Paths}

import com.spikerlabs.streamingapp.acquisition.MessageStream
import com.spikerlabs.streamingapp.domain.Message
import cucumber.api.scala.{EN, ScalaDsl}
import cucumber.api.PendingException
import fs2.Stream
import org.scalatest.{AppendedClues, Matchers}

class DataAcquisitionSteps extends ScalaDsl with EN with Matchers with AppendedClues {
  val filePathPrefix = "/tmp/streaming-app-example"

  def filePath(fileName: String): Path = Paths.get(s"$filePathPrefix-$fileName")

  Given("""^there is a clean state$""") { () =>
    sharedState = SharedState(Stream.empty)
  }

  Given("""^there is source file "([^"]*)":$""") { (fileName: String, fileContents: String) =>
    Files.write(filePath(fileName), fileContents.getBytes)
  }

  When("""^I create a message stream from source file "([^"]*)"$""") { fileName: String =>
    sharedState = sharedState.copy(stream = MessageStream.fromFile(filePath(fileName)))
  }

  Then("""^there should be an empty message stream$""") { () =>
    unsafeEvaluateStream()
    sharedState.maybeEvaluatedStream.get shouldBe empty
  }

  Then("""^there should be a stream with (\d+) messages$""") { expectedStreamSize: Int =>
    unsafeEvaluateStream()
    sharedState.maybeEvaluatedStream.get should have size expectedStreamSize
  }

  Then("""^all the messages in stream should be of type "([^"]*)"$""") { expectedMessageType: String =>
    unsafeEvaluateStream()
    sharedState.maybeEvaluatedStream.get.foreach { message =>
      expectedMessageType match {
        case "VisitCreate" => message shouldBe a[Message]
        case other => fail(s"unsupported message type: $other")
      }
    }
  }

  Given("""^there is a message stream from file "([^"]*)":$""") { (fileName: String, fileContents: String) =>
    Files.write(filePath(fileName), fileContents.getBytes)
    sharedState = sharedState.copy(stream = MessageStream.fromFile(filePath(fileName)))
  }

}
