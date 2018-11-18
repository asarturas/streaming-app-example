package com.spikerlabs.streamingapp.transport

import java.nio.file.Path

import cats.effect.{ContextShift, IO, Sync}
import com.spikerlabs.streamingapp.domain.Message
import com.spikerlabs.streamingapp.transport.MessageStream.MessageStream
import fs2.{io, text}
import _root_.io.circe.fs2.{decoder, stringStreamParser}

import scala.concurrent.ExecutionContextExecutorService

object MessageInput {

  def fromFile(filePath: Path)(implicit ec: ExecutionContextExecutorService, cs: ContextShift[IO]): MessageStream =
    io.file.readAll[IO](filePath, ec, 4096)
      .through(text.utf8Decode)
      .through(text.lines)
      .through(stringStreamParser[IO])
      .through(decoder[IO, Message])

}
