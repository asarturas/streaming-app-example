package com.spikerlabs.streamingapp.domain

import java.nio.file.Path

import cats.effect.IO
import fs2.Stream

object MessageStream {
  type MessageStream[F[_]] = Stream[F, Message]
  def fromFile(filePath: Path): MessageStream[IO] = Stream.empty
}
