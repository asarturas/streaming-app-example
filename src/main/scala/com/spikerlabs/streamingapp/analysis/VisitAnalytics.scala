package com.spikerlabs.streamingapp.analysis

import cats.kernel.Eq
import com.spikerlabs.streamingapp.domain.Message
import com.spikerlabs.streamingapp.domain.message.{DocumentVisitAnalytics, VisitCreate, VisitSummary, VisitUpdate}
import fs2.{Chunk, Pipe, Pull, Stream}

import scala.collection.SortedSet

object VisitAnalytics {
  def aggregateVisits[F[_]]: Pipe[F, Message, Message] = in =>
    in.buffer(1000).through(orderVisits(1, 1000)).through(toVisitSummaries).through(toDocumentVisitAnalytics)

  private[analysis] def orderVisits[F[_]](thresholdInMinutes: Int = 1, bufferSizeThreshold: Int = 10000): Pipe[F, Message, Message] = {
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

  private[analysis] def toVisitSummaries[F[_]]: Pipe[F, Message, VisitSummary] = {
    def go(buffer: VisitBuffer, s: Stream[F, Message]): Pull[F, VisitSummary, Option[Unit]] =
      s.pull.uncons.flatMap {
        case Some((chunk, s)) =>
          val c = chunk.toVector.foldLeft(Vector.empty[Message])((acc, msg) => acc :+ msg)
          buffer.add(c)
          //c.map(Message.date).foreach(println)
          Pull.output(Chunk.seq(buffer.flush(Message.date(c.last).minusHours(1)))) >> go(buffer, s)
        case None =>
          Pull.output(Chunk.seq(buffer.end())) >> Pull.pure(None)
//        case None => Pull.pure(None)
      }
    s => go(new VisitBuffer(), s).stream
  }

  private[analysis] def toDocumentVisitAnalytics[F[_]]: Pipe[F, VisitSummary, DocumentVisitAnalytics] = {
    def go(s: Stream[F, (Long, List[VisitSummary])]): Pull[F, DocumentVisitAnalytics, Option[Unit]] =
      s.pull.uncons.flatMap {
        case Some((mess, s)) =>
          Pull.output(Chunk.vector(DocumentVisitAnalytics.fromSummaries(mess.toList.head._2).toVector)) >> go(s)
        case None => Pull.pure(None)
      }
    s => go(s.through(groupSummariesByHour)).stream
  }

  private def groupSummariesByHour[F[_]]: Pipe[F, VisitSummary, (Long, List[VisitSummary])] =
    s => s.groupAdjacentBy { w =>
      //println(w.timePeriod.startTime.toEpochSecond)
      w.timePeriod.startTime.toEpochSecond
    }(Eq.fromUniversalEquals)
      .flatMap {
        case (x, v) => //println("adj -> " + v.size)
          Stream.emit((x -> v.toList))
      }
}
