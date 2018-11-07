package steps

import java.nio.file.{Files, Path, Paths}

import com.spikerlabs.streamingapp.domain.Message
import cucumber.api.scala.{EN, ScalaDsl}
import cucumber.api.PendingException
import fs2.Stream
import fs2.text._
import org.scalatest.{AppendedClues, Matchers}
import com.spikerlabs.streamingapp.analysis.DocumentVisitAnalytics._

class DataAnalysisSteps extends ScalaDsl with EN with Matchers with AppendedClues {

  When("""^I run analysis of the message stream$""") { () =>
    val analysis = sharedState.stream.through(aggregateVisits)
    sharedState = SharedState(stream = analysis)
  }

  Then("""^the generated analysis should match the expectation:$""") { expectation: String =>
    val preparedAnalysis = sharedState.stream
      .map(_.toString)
      .compile
      .toList
      .unsafeRunSync()
    preparedAnalysis.mkString("\n") shouldBe expectation withClue preparedAnalysis
  }

}