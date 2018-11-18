package com.spikerlabs.streamingapp.transport

import java.nio.file.Path

import _root_.io.circe.fs2.{decoder, stringStreamParser}
import cats.effect.{ContextShift, IO}
import com.spikerlabs.streamingapp.domain.Message
import fs2.{io, text, Stream}

import scala.concurrent.ExecutionContextExecutorService

object MessageStream {

  type MessageStream = Stream[IO, Message]

  implicit val ioContextShift: ContextShift[IO] = IO.contextShift(scala.concurrent.ExecutionContext.Implicits.global)

  def fromFile(filePath: Path)(implicit ec: ExecutionContextExecutorService): MessageStream =
    io.file.readAll[IO](filePath, ec, 4096)
      .through(text.utf8Decode)
      .through(text.lines)
      .through(stringStreamParser[IO])
      .through(decoder[IO, Message])
}
