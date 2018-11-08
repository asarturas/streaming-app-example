package com.spikerlabs.streamingapp.analysis

import com.spikerlabs.streamingapp.domain.Message
import fs2.{Chunk, Pipe, Pull, Stream}

import scala.collection.SortedSet

object DocumentVisitAnalytics {
  def aggregateVisits[F[_]]: Pipe[F, Message, Message] = in => in


  private[analysis] def orderByVisitDate[F[_]](thresholdInMinutes: Int = 1, bufferSizeThreshold: Int = 10000): Pipe[F, Message, Message] = {
    def order(buffer: SortedSet[Message], chunk: Chunk[Message]): (Seq[Message], SortedSet[Message]) = {
      val newData = chunk.toVector
      val timeout = newData.map(Message.date).maxBy(_.toInstant.toEpochMilli).minusMinutes(1)
      val (toOutput, newBuffer) = (buffer ++ chunk.toVector).partition(msg => Message.date(msg).isBefore(timeout))
      if (newBuffer.size >= bufferSizeThreshold) {
        val (bufferToFlush, bufferToKeep) = newBuffer.splitAt(newBuffer.size / 3)
        (toOutput.toSeq ++ bufferToFlush, bufferToKeep)
      } else {
        (toOutput.toSeq, newBuffer)
      }
    }
    def go(buffer: SortedSet[Message], s: Stream[F, Message]): Pull[F, Message, Option[Unit]] =
      s.pull.uncons.flatMap {
        case Some((chunk, s)) =>
          val (toOutput, newBuffer) = order(buffer, chunk)
          Pull.output(Chunk.seq(toOutput)) >> go(newBuffer, s)
        case None if buffer.nonEmpty =>
          Pull.output(Chunk.seq(buffer.toSeq)) >> Pull.pure(None)
        case None => Pull.pure(None)
      }
    s => go(SortedSet.empty[Message], s).stream
  }
}
