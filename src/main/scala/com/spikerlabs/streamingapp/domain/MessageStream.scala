package com.spikerlabs.streamingapp.domain

import java.nio.file.Path
import io.circe.fs2._
import io.circe.generic.auto._
import io.circe.syntax._
import cats.effect.IO
import fs2.{io, text, Stream}
import cats.implicits._
import cats.effect.ContextShift

import scala.concurrent.ExecutionContextExecutorService

object MessageStream {

  type MessageStream = Stream[IO, Message]

  implicit val ioContextShift: ContextShift[IO] = IO.contextShift(scala.concurrent.ExecutionContext.Implicits.global)

  def fromFile(filePath: Path)(implicit ec: ExecutionContextExecutorService): Stream[IO, Message] =
    io.file.readAll[IO](filePath, ec, 4096)
      .through(text.utf8Decode)
      .through(text.lines)
      .through(stringStreamParser[IO])
      .through(decoder[IO, Message])
}
