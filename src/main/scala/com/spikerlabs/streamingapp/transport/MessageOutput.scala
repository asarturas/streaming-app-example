package com.spikerlabs.streamingapp.transport

import java.nio.file.Path

import cats.effect.{ContextShift, Sync}
import com.spikerlabs.streamingapp.domain.Message
import fs2.{io, text}

import scala.concurrent.ExecutionContextExecutorService

object MessageOutput {

  type MessageOutput[F[_]] = fs2.Sink[F, Message]

  def writeToFile[F[_]: Sync: ContextShift](path: Path)(implicit ecs: ExecutionContextExecutorService): MessageOutput[F] =
    _
      .intersperse("\n")
      .map(_.toString)
      .through(text.utf8Encode)
      .through(io.file.writeAll(path, ecs))
}