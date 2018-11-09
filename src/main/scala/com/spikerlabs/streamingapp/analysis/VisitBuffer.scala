package com.spikerlabs.streamingapp.analysis

import com.spikerlabs.streamingapp.domain.Message
import com.spikerlabs.streamingapp.domain.message.{VisitCreate, VisitInProgress, VisitSummary, VisitUpdate}

class VisitBuffer(private var initialVisits: Vector[Message] = Vector.empty[Message]) {

  private var buffer: Vector[VisitInProgress] = Vector.empty

  add(initialVisits)

  def isEmpty: Boolean = buffer.isEmpty

  def add(visits: Vector[Message]): VisitBuffer = {
    visits.foreach {
      case msg: VisitCreate => buffer = buffer :+ VisitInProgress(msg)
      case msg: VisitUpdate =>
        buffer.find(_.create.id == msg.id).foreach { previous =>
          val index = buffer.indexOf(previous)
          buffer = buffer.updated(index, VisitInProgress(previous.create, Some(msg)))
        }
      case _ =>
    }
    this
  }

  def flush(): Vector[VisitSummary] = {
    val (bufferToFlush, bufferToKeep) = buffer.partition(isComplete)
    buffer = bufferToKeep
    bufferToFlush.map(toSummary)
  }

  private def isComplete(visit: VisitInProgress): Boolean =
    visit.lastUpdate.exists(_.completion >= 0.999)

  private def toSummary(visit: VisitInProgress): VisitSummary = visit match {
    case VisitInProgress(start: VisitCreate, Some(end: VisitUpdate)) => VisitSummary(start, end)
    case VisitInProgress(start: VisitCreate, _) => VisitSummary(start)
  }


  def end(): Vector[VisitSummary] =
    buffer.map {
      case VisitInProgress(start: VisitCreate, Some(end: VisitUpdate)) => VisitSummary(start, end) 
      case VisitInProgress(start: VisitCreate, _) => VisitSummary(start)
    }

}
