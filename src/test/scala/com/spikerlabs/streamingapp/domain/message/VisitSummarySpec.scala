package com.spikerlabs.streamingapp.domain.message

import java.time.ZonedDateTime
import java.util.UUID

import com.spikerlabs.streamingapp.domain.TimePeriod
import org.scalatest.{AppendedClues, FlatSpec, Matchers}

class VisitSummarySpec extends FlatSpec with Matchers with AppendedClues {

  behavior of "factory"

  it should "create a visit summary from a visit create alone" in {

    val create = VisitCreate(
      id = UUID.fromString("82abce83-3892-48ee-9f1b-d34c4746ace7"),
      userId = UUID.fromString("dc0ad841-0b89-4411-a033-d3f174e8d0ad"),
      documentId = UUID.fromString("7b2bc74e-f529-4f5d-885b-4377c424211d"),
      createdAt = ZonedDateTime.parse("2015-04-22T11:42:07.602Z")
    )

    VisitSummary(create) shouldBe VisitSummary(
      create.id,
      create.userId,
      create.documentId,
      TimePeriod(
        startTime = ZonedDateTime.parse("2015-04-22T11:00:00.000Z"),
        endTime = ZonedDateTime.parse("2015-04-22T12:00:00.000Z")
      ),
      time = 0,
      isCompleted = false
    )

  }

  it should "create a visit summary from a pair of incompete visit create and update" in {

    val create = VisitCreate(
      id = UUID.fromString("82abce83-3892-48ee-9f1b-d34c4746ace7"),
      userId = UUID.fromString("dc0ad841-0b89-4411-a033-d3f174e8d0ad"),
      documentId = UUID.fromString("7b2bc74e-f529-4f5d-885b-4377c424211d"),
      createdAt = ZonedDateTime.parse("2015-04-22T11:42:07.602Z")
    )

    val update = VisitUpdate(
      id = UUID.fromString("82abce83-3892-48ee-9f1b-d34c4746ace7"),
      engagedTime = 25,
      completion = 0.4,
      updatedAt = ZonedDateTime.parse("2015-04-22T12:40:35.122Z")
    )

    VisitSummary(create, update) shouldBe VisitSummary(
      create.id,
      create.userId,
      create.documentId,
      TimePeriod(
        startTime = ZonedDateTime.parse("2015-04-22T12:00:00.000Z"),
        endTime = ZonedDateTime.parse("2015-04-22T13:00:00.000Z")
      ),
      time = update.engagedTime,
      isCompleted = false
    )

  }

  it should "create a visit summary from a pair of complete visit create and update" in {

    val create = VisitCreate(
      id = UUID.fromString("82abce83-3892-48ee-9f1b-d34c4746ace7"),
      userId = UUID.fromString("dc0ad841-0b89-4411-a033-d3f174e8d0ad"),
      documentId = UUID.fromString("7b2bc74e-f529-4f5d-885b-4377c424211d"),
      createdAt = ZonedDateTime.parse("2015-04-22T11:42:07.602Z")
    )

    val update = VisitUpdate(
      id = UUID.fromString("82abce83-3892-48ee-9f1b-d34c4746ace7"),
      engagedTime = 25,
      completion = 0.9999,
      updatedAt = ZonedDateTime.parse("2015-04-22T12:40:35.122Z")
    )

    VisitSummary(create, update) shouldBe VisitSummary(
      create.id,
      create.userId,
      create.documentId,
      TimePeriod(
        startTime = ZonedDateTime.parse("2015-04-22T12:00:00.000Z"),
        endTime = ZonedDateTime.parse("2015-04-22T13:00:00.000Z")
      ),
      time = update.engagedTime,
      isCompleted = true
    )

  }

}
