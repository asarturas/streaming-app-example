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

    buffer.end() shouldBe Seq(
      VisitSummary(VisitCreate(first, first, first, earliestDate)),
      VisitSummary(VisitCreate(second, second, second, earlierDate)),
    )
  }

  it should "update existing visit with update" in {
    val buffer = new VisitBuffer(initialVisits).add(
      Vector(VisitUpdate(first, 1, 1, earlierDate))
    )

    buffer.end() shouldBe Seq(
      VisitSummary(VisitCreate(first, first, first, earliestDate), VisitUpdate(first, 1, 1, earlierDate)),
      VisitSummary(VisitCreate(second, second, second, earlierDate)),
    )
  }

  it should "update existing visit with a more recent update" in {
    val buffer = new VisitBuffer(initialVisits).add(
      Vector(
        VisitUpdate(first, 1, 0.2, earlierDate),
        VisitUpdate(first, 2, 0.4, laterDate),
        VisitUpdate(first, 3, 1.0, laterDate)
      )
    )

    buffer.end() shouldBe Seq(
      VisitSummary(VisitCreate(first, first, first, earlierDate), VisitUpdate(first, 3, 1.0, laterDate)),
      VisitSummary(VisitCreate(second, second, second, earlierDate))
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
        VisitUpdate(first, 1, 0.3, laterDate),
        VisitUpdate(second, 3600, 0.1, earliestDate.plusHours(1)),
        VisitCreate(third, third, third, earliestDate.plusHours(2))
      )
    )

    buffer.flush(earliestDate.plusHours(1)) shouldBe Seq(
      VisitSummary(VisitCreate(first, first, first, earliestDate), VisitUpdate(first, 1, 0.3, laterDate)),
      VisitSummary(VisitCreate(second, second, second, laterDate), VisitUpdate(second, 3600, 0.1, earliestDate.plusHours(1))),
    )
    buffer.end() shouldBe Seq(
      VisitSummary(VisitCreate(third, third, third, earliestDate.plusHours(2))),
    )
  }

  it should "update two visits in parallel and flush them both at timeout" in {
    val buffer = new VisitBuffer(initialVisits)
      .add(Vector(VisitUpdate(first, 1, 0.2, earlierDate)))
      .add(Vector(VisitUpdate(second, 1, 0.3, earlierDate)))
      .add(Vector(VisitUpdate(first, 2, 0.4, laterDate)))
      .add(Vector(VisitUpdate(second, 1, 0.6, laterDate)))
      .add(Vector(VisitUpdate(second, 3, 0.9, latestDate)))
      .add(Vector(VisitUpdate(first, 3, 0.6, latestDate)))

    buffer.flush(latestDate) should contain theSameElementsInOrderAs Seq(
      VisitSummary(VisitCreate(first, first, first, earliestDate), VisitUpdate(first, 3, 0.6, latestDate)),
      VisitSummary(VisitCreate(second, second, second, earlierDate), VisitUpdate(second, 3, 0.9, latestDate)),
    )

    buffer.end() shouldBe empty
  }

  it should "gradually flush matching records from the buffer" in {
    val buffer = new VisitBuffer(initialVisits)
      .add(
        Vector(
          VisitCreate(third, third, third, laterDate),
          VisitCreate(fourth, fourth, fourth, latestDate)
        )
      )

    buffer.flush(earliestDate) shouldBe Seq(VisitSummary(VisitCreate(first, first, first, earliestDate))) withClue buffer.end()

    buffer.flush(earlierDate) shouldBe Seq(VisitSummary(VisitCreate(second, second, second, earlierDate))) withClue buffer.end()

    buffer.flush(laterDate) shouldBe Seq(VisitSummary(VisitCreate(third, third, third, laterDate))) withClue buffer.end()

    buffer.end() shouldBe Seq(VisitSummary(VisitCreate(fourth, fourth, fourth,latestDate)))
  }

}
