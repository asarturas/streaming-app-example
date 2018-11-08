package com.spikerlabs.streamingapp.domain.message

import java.time.ZonedDateTime

import org.scalatest.{AppendedClues, FlatSpec, Matchers}
import java.util.UUID

import com.spikerlabs.streamingapp.domain.TimePeriod

class DocumentVisitAnalyticsSpec extends FlatSpec with Matchers with AppendedClues {

  it should "serialise to a string" in {
    val analytics = DocumentVisitAnalytics(
      documentId = UUID.fromString("12345678-1234-1234-1234-123456781234"),
      TimePeriod(
        startTime = ZonedDateTime.parse("2015-01-01T00:00Z"),
        endTime = ZonedDateTime.parse("2015-01-01T00:00Z"),
      ),
      visits = 1,
      uniques = 2,
      time = 3.4,
      completion = 5
    )

    analytics.toString shouldBe "12345678-1234-1234-1234-123456781234|2015-01-01T00:00Z|2015-01-01T00:00Z|1|2|3.4|5"
  }

  behavior of "factory"

  it should "return a list of analytics for list of visit summaries" in {
    def random = () => UUID.randomUUID()
    val first = UUID.nameUUIDFromBytes("1".getBytes())
    val second = UUID.nameUUIDFromBytes("2".getBytes())
    val third = UUID.nameUUIDFromBytes("3".getBytes())
    val onePeriod = TimePeriod(ZonedDateTime.parse("2015-01-01T00:00Z"), ZonedDateTime.parse("2015-01-01T01:00Z"))
    val otherPeriod = TimePeriod(ZonedDateTime.parse("2015-01-01T01:00Z"), ZonedDateTime.parse("2015-01-01T02:00Z"))
    val summaries = List(
      VisitSummary(random(), first, first, onePeriod, 900, isCompleted = false),
      VisitSummary(random(), first, first, onePeriod, 900, isCompleted = false),
      VisitSummary(random(), first, second, onePeriod, 900, isCompleted = false),
      VisitSummary(random(), first, third, onePeriod, 900, isCompleted = true),
      VisitSummary(random(), first, first, otherPeriod, 900, isCompleted = false),
      VisitSummary(random(), second, first, otherPeriod, 900, isCompleted = true),
      VisitSummary(random(), third, first, otherPeriod, 900, isCompleted = false),
    )
    DocumentVisitAnalytics.fromSummaries(summaries) should contain allElementsOf List(
      DocumentVisitAnalytics(first, onePeriod, 2, 1, 0.5, 0),
      DocumentVisitAnalytics(second, onePeriod, 1, 1, 0.25, 0),
      DocumentVisitAnalytics(third, onePeriod, 1, 1, 0.25, 1),
      DocumentVisitAnalytics(first, otherPeriod, 3, 3, 0.75, 1),
    )
  }

}
