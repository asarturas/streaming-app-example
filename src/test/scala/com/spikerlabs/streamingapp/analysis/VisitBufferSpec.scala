package com.spikerlabs.streamingapp.analysis

import java.time.ZonedDateTime
import java.util.UUID

import com.spikerlabs.streamingapp.domain.message.{VisitCreate, VisitSummary, VisitUpdate}
import org.scalatest.{AppendedClues, FlatSpec, Matchers}

class VisitBufferSpec extends FlatSpec with Matchers with AppendedClues {

  val first = UUID.nameUUIDFromBytes("1".getBytes())
  val second = UUID.nameUUIDFromBytes("2".getBytes())
  val third = UUID.nameUUIDFromBytes("3".getBytes())
  val fourth = UUID.nameUUIDFromBytes("4".getBytes())
  val earliestDate = ZonedDateTime.parse("2015-01-01T00:00Z")
  val earlierDate = ZonedDateTime.parse("2015-01-01T00:01Z")
  val laterDate = ZonedDateTime.parse("2015-01-01T00:02Z")
  val latestDate = ZonedDateTime.parse("2015-01-01T00:03Z")
  val initialVisits = Vector(
      VisitCreate(first, first, first, earliestDate),
      VisitCreate(second, second, second, earlierDate),
    )

  it should "indicate when it is empty" in {
    new VisitBuffer().isEmpty shouldBe true
  }

  it should "indicate when it is not empty" in {
    new VisitBuffer(initialVisits).isEmpty shouldBe false
  }

  it should "return all the buffer at the end" in {

    val buffer = new VisitBuffer(initialVisits)

    buffer.end() shouldBe Vector(
      VisitSummary(VisitCreate(first, first, first, earliestDate)),
      VisitSummary(VisitCreate(second, second, second, earlierDate)),
    )
  }

  it should "update existing visit with update" in {
    val buffer = new VisitBuffer(initialVisits).add(
      Vector(VisitUpdate(first, 1, 1, earlierDate))
    )

    buffer.end() shouldBe Vector(
      VisitSummary(VisitCreate(first, first, first, earliestDate), VisitUpdate(first, 1, 1, earlierDate)),
      VisitSummary(VisitCreate(second, second, second, earlierDate)),
    )
  }

  it should "discard sole update without create before it" in {
    val buffer = new VisitBuffer().add(
      Vector(VisitUpdate(third, 1, 1, earlierDate))
    )

    buffer.end() shouldBe Vector()
  }

  it should "flush visits which are 1 hour old or older" in {
    val buffer = new VisitBuffer(initialVisits).add(
      Vector(
        VisitUpdate(first, 1, 0.2, earlierDate),
        VisitUpdate(second, 3600, 0.1, earliestDate.plusHours(1)),
        VisitCreate(third, third, third, earliestDate.plusHours(2))
      )
    )

    buffer.flush() shouldBe Vector(
      VisitSummary(VisitCreate(first, first, first, earliestDate), VisitUpdate(first, 1, 0.2, earlierDate)),
      VisitSummary(VisitCreate(second, second, second, laterDate), VisitUpdate(second, 3600, 0.1, earliestDate.plusHours(1))),
    )
    buffer.end() shouldBe Vector(
      VisitSummary(VisitCreate(third, third, third, earliestDate.plusHours(2))),
    )
  }

}
