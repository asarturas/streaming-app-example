package com.spikerlabs.streamingapp.domain.message

import java.util.UUID

import com.spikerlabs.streamingapp.domain.{Message, TimePeriod}

case class DocumentVisitAnalytics(documentId: UUID, timePeriod: TimePeriod, visits: Int, uniques: Int, time: Double, completion: Int) extends Message {
  override def toString: String =
    documentId + "|" + timePeriod.startTime + "|" + timePeriod.endTime + "|" + visits + "|" + uniques + "|" + time + "|" + completion
}

object DocumentVisitAnalytics {

  def fromSummaries(summaries: List[VisitSummary]): List[DocumentVisitAnalytics] = {
    val groupedSummaries = summaries.groupBy(_.documentId).map {
      case (documentId, documentVisits) => (documentId, documentVisits.groupBy(_.timePeriod))
    }
    //groupedSummaries.foreach(x => println("gr -> " + x._2.size))
    groupedSummaries.flatMap {
      case (documentId, groupedDocumentVisits) => groupedDocumentVisits.map {
        case (timePeriod, documentVisitsForPeriod) =>
          val users = documentVisitsForPeriod.map(_.userId)
          val cumulativeTimeInHours = documentVisitsForPeriod.map(_.time).sum.toDouble / 3600
          val numberOfCompletions = documentVisitsForPeriod.count(_.isCompleted)
          DocumentVisitAnalytics(documentId, timePeriod, users.length, users.distinct.length, cumulativeTimeInHours, numberOfCompletions)
      }
    }.toList
  }

}