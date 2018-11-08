package com.spikerlabs.streamingapp.domain

import java.time.ZonedDateTime
import java.util.UUID

import com.spikerlabs.streamingapp.domain.message.{VisitCreate, VisitUpdate}
import org.scalatest.{AppendedClues, FlatSpec, Matchers}

class TimePeriodSpec extends FlatSpec with Matchers with AppendedClues {

  behavior of "factory"

  it should "create time period based on visit update message" in {
    val message = VisitUpdate(
      id = UUID.fromString("82abce83-3892-48ee-9f1b-d34c4746ace7"),
      engagedTime = 25,
      completion = 0.4,
      updatedAt = ZonedDateTime.parse("2015-04-22T11:42:35.122Z")
    )
    TimePeriod.fromVisitUpdate(message) shouldBe TimePeriod(
      startTime = ZonedDateTime.parse("2015-04-22T11:00:00.000Z"),
      endTime = ZonedDateTime.parse("2015-04-22T12:00:00.000Z")
    )
  }

  it should "create time period based on visit create message" in {
    val message = VisitCreate(
      id = UUID.fromString("82abce83-3892-48ee-9f1b-d34c4746ace7"),
      userId = UUID.fromString("dc0ad841-0b89-4411-a033-d3f174e8d0ad"),
      documentId = UUID.fromString("7b2bc74e-f529-4f5d-885b-4377c424211d"),
      createdAt = ZonedDateTime.parse("2015-04-22T11:42:07.602Z")
    )
    TimePeriod.fromVisitCreate(message) shouldBe TimePeriod(
      startTime = ZonedDateTime.parse("2015-04-22T11:00:00.000Z"),
      endTime = ZonedDateTime.parse("2015-04-22T12:00:00.000Z")
    )
  }

}
