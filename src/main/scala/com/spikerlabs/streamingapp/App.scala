package com.spikerlabs.streamingapp

import java.nio.file.Paths
import java.util.concurrent.Executors

import cats.effect.{ExitCode, IO, IOApp, Resource}
import cats.implicits._
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

  def fahrenheitToCelsius(f: DataPoint): OutSomething =
      OutSomething(c = (f.far - 32.0) * (5.0 / 9.0), f = f.far)

  def run(args: List[String]): IO[ExitCode] = {
    blockingResource.use { blockingExecutionContext: ExecutionContextExecutorService =>
      io.file.readAll[IO](Paths.get("testdata/fahrenheit.txt"), blockingExecutionContext, 4096)
        .through(text.utf8Decode)
        .through(text.lines)
        .filter(s => !s.trim.isEmpty && !s.startsWith("//"))
        .through(stringStreamParser)
        .through(decoder[IO, DataPoint])
        .map(fahrenheitToCelsius)
        .map(_.asJson.noSpaces)
        .intersperse("\n")
        .through(text.utf8Encode)
        .through(io.file.writeAll(Paths.get("testdata/celsius.txt"), blockingExecutionContext))
        .compile.drain
        .as(ExitCode.Success)
    }
  }
}
