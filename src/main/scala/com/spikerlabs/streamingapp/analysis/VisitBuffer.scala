package com.spikerlabs.streamingapp.analysis

import java.time.ZonedDateTime
import java.util.UUID

import com.spikerlabs.streamingapp.domain.Message
import com.spikerlabs.streamingapp.domain.message.{VisitCreate, VisitInProgress, VisitSummary, VisitUpdate}

import scala.collection.mutable.SortedMap

class VisitBuffer(private var initialVisits: Vector[Message] = Vector.empty[Message]) {

  private var buffer: SortedMap[UUID, VisitInProgress] = SortedMap.empty
  private var dateToIdIndex: SortedMap[Long, List[UUID]] = SortedMap.empty

  add(initialVisits)

  def isEmpty: Boolean = buffer.isEmpty

  def add(visits: Vector[Message]): VisitBuffer = {
    visits.foreach {
      case msg: VisitCreate =>
        buffer.put(msg.id, VisitInProgress(msg))
        addToDateIndex(msg.createdAt, msg.id)
      case msg: VisitUpdate =>
        buffer.get(msg.id).foreach { previous =>
          buffer.put(msg.id, VisitInProgress(previous.create, Some(msg)))
        }
      case _ =>
    }
    this
  }

  private def addToDateIndex(date: ZonedDateTime, id: UUID): Unit = {
    val time = date.toInstant.getEpochSecond
    dateToIdIndex.get(time) match {
      case None => dateToIdIndex.put(time, List(id))
      case Some(list) => dateToIdIndex.update(time, id :: list)
    }
  }

  def flush(timeout: ZonedDateTime): Seq[VisitSummary] = {
    val timeoutSeconds = timeout.toInstant.getEpochSecond
    // filter is quite expensive so quick check to see if we can avoid it
    if (dateToIdIndex.firstKey > timeoutSeconds) Seq.empty
    val datesToFlush = dateToIdIndex.filterKeys(x => x <= timeoutSeconds)
    val idsToFlush = datesToFlush.flatMap(_._2)
    if (idsToFlush.nonEmpty) {
      // effectful flat map is not idiomatic, but it saves extra iteration
      val itemsToFlush = idsToFlush.flatMap { id =>
        val item = buffer.get(id)
        buffer.remove(id)
        item
      }
      // cleanup every now and then - it's expensive so not doing on each flush
      if (dateToIdIndex.size > buffer.size) {
        dateToIdIndex = dateToIdIndex.filter(x => buffer.contains(x._2.head))
      }
      itemsToFlush.map(toSummary).toSeq
    } else {
      Seq.empty
    }
  }

  private def toSummary(visit: VisitInProgress): VisitSummary = visit match {
    case VisitInProgress(start: VisitCreate, Some(end: VisitUpdate)) => VisitSummary(start, end)
    case VisitInProgress(start: VisitCreate, _) => VisitSummary(start)
  }

  def end(): Seq[VisitSummary] = {
    val ordered: Seq[VisitInProgress] = dateToIdIndex.values.flatten.flatMap(buffer.get).toSeq
    val theRest = buffer.values.filterNot(ordered.contains).toSeq
    (ordered ++ theRest).map(toSummary)
  }
}
