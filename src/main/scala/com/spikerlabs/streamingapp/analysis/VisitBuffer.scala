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
      case Some(list) => dateToIdIndex.put(time, id :: list)
    }
  }

  def flush(timeout: ZonedDateTime): Seq[VisitSummary] = {
    val te = timeout.toInstant.getEpochSecond
//    val x: (Long, List[UUID]) = dateToIdIndex.maxBy(_._1 < te)
//    UUID.from
//    val datesToFlush = dateToIdIndex.takeWhile(_ != x)
    if (dateToIdIndex.firstKey >= te) Seq.empty
    val datesToFlush = dateToIdIndex.filterKeys(x => x <= te)
//    val datesToFlush = dateToIdIndex.filterKeys(_ < te)//dateToIdIndex.get(te) match {
//      case Some(id) => dateToIdIndex.splitAt(dateToIdIndex.zipWithIndex.getOrElse((te, id), 0))._1
//      case None => dateToIdIndex.takeWhile(_._1 < te)
//    }
      val idsToFlush = datesToFlush.values.flatten
      if (idsToFlush.nonEmpty) {
//        println("flush " + buffer.size + " " + dateToIdIndex.size)
        val itemsToFlush = idsToFlush.flatMap(buffer.get)
        idsToFlush.foreach { id =>
          buffer.remove(id)
        }
        if (dateToIdIndex.size > buffer.size) {
//          buffer = buffer.foldLeft(SortedMap.empty[UUID, VisitInProgress]) { (acc, elem) =>
//            if (idsToFlush.exists(_ != elem._1)) acc.put(elem._1, elem._2)
//            acc
//          }
          dateToIdIndex = dateToIdIndex.filterNot(x => buffer.contains(x._2.head))
//          println("after clean " + buffer.size + " " + dateToIdIndex.size)
        } else {

        }


        val d = itemsToFlush.map(toSummary).toSeq
        //println("data")
        //d.foreach(println)
        d
      } else {
        Seq.empty
      }
  }

  private def isExpired(visit: VisitInProgress)(implicit timeout: ZonedDateTime): Boolean =
    visit.create.createdAt.isBefore(timeout)


  private def toSummary(visit: VisitInProgress): VisitSummary = visit match {
    case VisitInProgress(start: VisitCreate, Some(end: VisitUpdate)) => VisitSummary(start, end)
    case VisitInProgress(start: VisitCreate, _) => VisitSummary(start)
  }


  def end(): Seq[VisitSummary] = {
//    println("size = " + buffer.size)
//    flush(buffer.head._2.create.createdAt.minusHours(2))
    //    buffer.values.map(toSummary).toSeq
    //    if (buffer.nonEmpty) flush(buffer.last._2.create.createdAt) ++ dateToIdIndex.values.flatMap(x => x.flatMap(buffer.get)).map(toSummary).toSeq
    //    else Seq.empty
//    println("x = " + x.size + " buff = " + buffer.size + " date = " + dateToIdIndex.size)
//    x //++ buffer.filterNot(y => x.exists(_.id == y)).values.map(toSummary)
//    buffer.values.map(toSummary).toSeq
    //    val ordered: Seq[VisitInProgress] = dateToIdIndex.values.flatMap(x => x.flatMap(buffer.get)).toSeq//.map(toSummary).toSeq
val ordered: Seq[VisitInProgress] = dateToIdIndex.values.flatten.flatMap(buffer.get).toSeq//.map(toSummary).toSeq
        val theRest = buffer.values.filterNot(ordered.contains).toSeq
    (ordered ++ theRest).map(toSummary)
//    ordered.map(toSummary)
  }
}
