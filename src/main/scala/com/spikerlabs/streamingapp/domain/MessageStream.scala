package com.spikerlabs.streamingapp.domain

import java.nio.file.Path
import io.circe.fs2._
import io.circe.generic.auto._
import io.circe.syntax._
import cats.effect.{ContextShift, IO, Sync}
import fs2.{io, text, Stream}

import scala.concurrent.ExecutionContextExecutorService

object MessageStream {

  type MessageStream[F[_]] = Stream[F, Message]

//  def fromFile[F[_]: Sync: ContextShift](filePath: Path)(implicit blockingExecutionContext: ExecutionContextExecutorService): MessageStream[IO] =
  def fromFile[F[_]](filePath: Path)(implicit blockingExecutionContext: ExecutionContextExecutorService): MessageStream[IO] =
    Stream.empty
//    io.file.readAll(filePath, blockingExecutionContext, 4096)
//      .through(text.utf8Decode)
//      .through(text.lines)
//      .through(stringStreamParser)
//      .map(_.)
//      .through(decoder[IO, Message])
}
