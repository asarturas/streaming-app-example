package com.spikerlabs.streamingapp

import java.nio.file.Paths
import java.util.concurrent.Executors

import cats.effect.{ExitCode, IO, IOApp, Resource}
import cats.implicits._
import com.spikerlabs.streamingapp.transport.{MessageInput, MessageOutput, MessageStream}
import com.spikerlabs.streamingapp.analysis.VisitAnalytics
import com.spikerlabs.streamingapp.domain.Message
import fs2.{io, text}

import scala.concurrent.{ExecutionContext, ExecutionContextExecutorService}

object App extends IOApp {
  val blockingResource: Resource[IO, ExecutionContextExecutorService] =
    Resource.make(
      IO(ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(2))) // set up
    )(
      es => IO(es.shutdown()) // tear down
    )

  def run(args: List[String]): IO[ExitCode] = {
    val (input, output) = args match {
      case oneParameter :: otherParameter :: _ => (oneParameter, otherParameter)
      case _ => throw new Exception("please provide input file and output file paths as two parameters for an app")
    }
    blockingResource.use { blockingExecutionContext: ExecutionContextExecutorService =>
      implicit val ec = blockingExecutionContext
      MessageInput.fromFile(Paths.get(input))
        .through(VisitAnalytics.aggregateVisits)
        .observe(MessageOutput.writeToFile(Paths.get(output)))
        .compile.drain
        .as(ExitCode.Success)
    }
  }
}