package com.spikerlabs.streamingapp

import java.nio.file.Paths
import java.util.concurrent.Executors

import cats.effect.{ExitCode, IO, IOApp, Resource}
import cats.implicits._
import com.spikerlabs.streamingapp.acquisition.MessageStream
import com.spikerlabs.streamingapp.analysis.VisitAnalytics
import com.spikerlabs.streamingapp.domain.message.DocumentVisitAnalytics
import io.circe.fs2._
import io.circe.generic.semiauto._
import io.circe.syntax._
import fs2.{io, text}

import scala.concurrent.{ExecutionContext, ExecutionContextExecutorService}

case class DataPoint(far: Double)

object DataPoint {
  implicit val decoder = deriveDecoder[DataPoint]
}

case class OutSomething(c: Double, f: Double)

object OutSomething {
  implicit val encoder = deriveEncoder[OutSomething]
}

object App extends IOApp {

  val blockingResource: Resource[IO, ExecutionContextExecutorService] =
      Resource.make(
        IO(ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(2))) // set up
      )(
        es => IO(es.shutdown()) // tear down
      )

  def run(args: List[String]): IO[ExitCode] = {
    blockingResource.use { blockingExecutionContext: ExecutionContextExecutorService =>
      MessageStream.fromFile(Paths.get("testdata/test-visit-messages.log"))(blockingExecutionContext)
        .through(VisitAnalytics.aggregateVisits)
        .through(_.evalMap{ documentAnalytics => IO { println(documentAnalytics); documentAnalytics }} )
        .compile.drain
        .as(ExitCode.Success)
    }
  }
}
