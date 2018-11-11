package com.spikerlabs.streamingapp.analysis

import java.time.ZonedDateTime
import java.util.UUID

import com.spikerlabs.streamingapp.domain.Message
import com.spikerlabs.streamingapp.domain.message.{VisitCreate, VisitInProgress, VisitSummary, VisitUpdate}

import scala.collection.concurrent.TrieMap
import scala.collection.mutable.SortedMap
import scala.collection.SortedSet

class VisitBuffer(private var initialVisits: Vector[Message] = Vector.empty[Message]) {

  private var buffer: SortedMap[UUID, VisitInProgress] = SortedMap.empty
  private implicit object A extends Ordering[ZonedDateTime] {
    def compare(o1: ZonedDateTime, o2: ZonedDateTime) =
      o1.isBefore(o2) match {
        case true => -1
        case false => 1
      }
  }
  private var dateIndex: SortedMap[ZonedDateTime, UUID] = SortedMap.empty

  add(initialVisits)

  def isEmpty: Boolean = buffer.isEmpty

  def add(visits: Vector[Message]): VisitBuffer = {
    visits.foreach {
      case msg: VisitCreate =>
        buffer.put(msg.id, VisitInProgress(msg))
        dateIndex.put(msg.createdAt, msg.id)
      case msg: VisitUpdate =>
        buffer.get(msg.id).foreach { previous =>
          buffer.put(msg.id, VisitInProgress(previous.create, Some(msg)))
        }
      case _ =>
    }
    this
  }

  def flush(timeout: ZonedDateTime): Vector[VisitSummary] = {
    val datesToFlush = dateIndex.takeWhile(_._1.isBefore(timeout))
      val idsToFlush = datesToFlush.values
      if (idsToFlush.nonEmpty) {
        val itemsToFlush = idsToFlush.flatMap(buffer.get)
//        dateIndex = dateIndex.filterNot(x => idsToFlush.toSeq.contains(x._2))
        idsToFlush.foreach(buffer.remove)
//        buffer = idsToFlush.map(buffer.get).to
        dateIndex = buffer.map {
          case (id, visitInProgress) => (visitInProgress.create.createdAt, id)
        }
//        buffer = buffer.(q => !idsToFlush.toSeq.contains(q._1))
//        dateIndex = dateIndex.filterNot(x => buffer.contains(x._2))
        val d = itemsToFlush.map(toSummary).toVector
        //println("data")
        //d.foreach(println)
        d
      } else {
        Vector.empty
      }
  }

  private def isExpired(visit: VisitInProgress)(implicit timeout: ZonedDateTime): Boolean =
    visit.create.createdAt.isBefore(timeout)


  private def toSummary(visit: VisitInProgress): VisitSummary = visit match {
    case VisitInProgress(start: VisitCreate, Some(end: VisitUpdate)) => VisitSummary(start, end)
    case VisitInProgress(start: VisitCreate, _) => VisitSummary(start)
  }


  def end(): Vector[VisitSummary] = {
    dateIndex.values.flatMap(buffer.get).map(toSummary).toVector
  }
}
