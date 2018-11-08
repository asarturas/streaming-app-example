package com.spikerlabs.streamingapp.domain.message

import java.util.UUID

import scala.concurrent.duration._

import com.spikerlabs.streamingapp.domain.{Message, TimePeriod}

case class VisitSummary(id: UUID, userId: UUID, documentId: UUID, timePeriod: TimePeriod, time: Int, isCompleted: Boolean) extends Message

object VisitSummary {

  def apply(visit: VisitCreate): VisitSummary =
    VisitSummary(
      visit.id,
      visit.userId,
      visit.documentId,
      TimePeriod.fromVisitCreate(visit),
      0,
      isCompleted = false
    )

  def apply(visitCreate: VisitCreate, visitUpdate: VisitUpdate): VisitSummary =
    VisitSummary(
      visitCreate.id,
      visitCreate.userId,
      visitCreate.documentId,
      TimePeriod.fromVisitUpdate(visitUpdate),
      time = visitUpdate.engagedTime,
      isCompleted = 1.0 - visitUpdate.completion < 0.001
    )

}