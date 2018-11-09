package com.spikerlabs.streamingapp.analysis

import java.time.ZonedDateTime
import java.util.UUID

import com.spikerlabs.streamingapp.domain.message.{DocumentVisitAnalytics, VisitCreate, VisitSummary, VisitUpdate}
import org.scalatest.{AppendedClues, FlatSpec, Matchers}
import fs2.{Chunk, Pure, Stream}

class VisitAnalyticsSpec extends FlatSpec with Matchers with AppendedClues {

  val first = UUID.nameUUIDFromBytes("1".getBytes())
  val second = UUID.nameUUIDFromBytes("2".getBytes())
  val third = UUID.nameUUIDFromBytes("3".getBytes())
  val fourth = UUID.nameUUIDFromBytes("4".getBytes())
  val earliestDate = ZonedDateTime.parse("2015-01-01T00:00Z")
  val earlierDate = ZonedDateTime.parse("2015-01-01T00:01Z")
  val laterDate = ZonedDateTime.parse("2015-01-01T00:02Z")
  val latestDate = ZonedDateTime.parse("2015-01-01T00:03Z")

  behavior of "orderVisits pipe"

  it should "not reorder already ordered stream" in {
    val stream = Stream(
      VisitCreate(first, first, first, earliestDate),
      VisitUpdate(first, 1, 1, earlierDate),
      VisitCreate(second, second, second, laterDate),
      VisitUpdate(second, 1, 1, latestDate),
    )

    stream.through(VisitAnalytics.orderVisits()).toList should
      contain theSameElementsInOrderAs stream.toList
  }

  it should "reorder items out of order, when they are within the threshold" in {
    val stream = Stream(
      VisitUpdate(first, 1, 1, earlierDate),
      VisitCreate(first, first, first, earliestDate),
      VisitUpdate(second, 1, 1, latestDate),
      VisitCreate(second, second, second, laterDate),
    )

    stream.through(VisitAnalytics.orderVisits(2)).toList should contain theSameElementsInOrderAs
      List(
        VisitCreate(first, first, first, earliestDate),
        VisitUpdate(first, 1, 1, earlierDate),
        VisitCreate(second, second, second, laterDate),
        VisitUpdate(second, 1, 1, latestDate),
      )
  }
  it should "only reorder items in chunks within the threshold" in {
    val stream = Stream.chunk(Chunk.seq(Seq(VisitCreate(second, second, second, laterDate)))) ++
      Stream.chunk(Chunk.seq(Seq(VisitUpdate(first, 1, 1, earlierDate)))) ++
      Stream.chunk(Chunk.seq(Seq(VisitUpdate(second, 1, 1, latestDate)))) ++
      Stream.chunk(Chunk.seq(Seq(VisitCreate(first, first, first, earliestDate))))

    stream.through(VisitAnalytics.orderVisits(2)).toList should contain theSameElementsInOrderAs
      List(
        VisitUpdate(first, 1, 1, earlierDate), // this is released first when laterDate mes arrives, making this to pass the threshold
        VisitCreate(first, first, first, earliestDate),
        VisitCreate(second, second, second, laterDate),
        VisitUpdate(second, 1, 1, latestDate),
      )
  }

  it should "flush 1/3 of the buffer then it grows too large" in {
    val stream = Stream.chunk(Chunk.seq(Seq(VisitUpdate(second, 1, 1, latestDate)))) ++
      Stream.chunk(Chunk.seq(Seq(VisitCreate(second, second, second, laterDate)))) ++
      Stream.chunk(Chunk.seq(Seq(VisitUpdate(first, 1, 1, earlierDate)))) ++
      Stream.chunk(Chunk.seq(Seq(VisitCreate(first, first, first, earliestDate))))

    stream.through(VisitAnalytics.orderVisits(2, 3)).toList should contain theSameElementsInOrderAs
      List(
        VisitUpdate(first, 1, 1, earlierDate), // this is released when buffer size reaches the threshold when laterDate message arrives
        VisitCreate(first, first, first, earliestDate),
        VisitCreate(second, second, second, laterDate),
        VisitUpdate(second, 1, 1, latestDate),
      )
  }

  behavior of "toVisitSummary pipe"

  it should "turn visits into visit summaries" in {
    val stream = Stream.chunk(Chunk.seq(Seq(VisitCreate(second, second, second, earliestDate)))) ++
      Stream.chunk(Chunk.seq(Seq(VisitCreate(first, first, first, earliestDate)))) ++
      Stream.chunk(Chunk.seq(Seq(VisitUpdate(second, 1, 0.5, earlierDate)))) ++
      Stream.chunk(Chunk.seq(Seq(VisitUpdate(first, 1, 0.5, earlierDate)))) ++
      Stream.chunk(Chunk.seq(Seq(VisitUpdate(second, 2, 0.5, laterDate)))) ++
      Stream.chunk(Chunk.seq(Seq(VisitUpdate(first, 2, 1.0, latestDate)))) ++
      Stream.chunk(Chunk.seq(Seq(VisitCreate(third, first, first, latestDate.plusHours(1))))) ++
      Stream.chunk(Chunk.seq(Seq(VisitCreate(fourth, first, first, latestDate.plusHours(3)))))

    stream.through(VisitAnalytics.toVisitSummaries).toList should contain theSameElementsInOrderAs
      List(
        VisitSummary(VisitCreate(second, second, second, earliestDate), VisitUpdate(second, 2, 0.5, laterDate)),
        VisitSummary(VisitCreate(first, first, first, earliestDate), VisitUpdate(first, 2, 1.0, latestDate)),
        VisitSummary(VisitCreate(third, first, first, latestDate.plusHours(1))),
        VisitSummary(VisitCreate(fourth, first, first, latestDate.plusHours(3)))
      )
  }

  behavior of "toDocumentVisitAnalytics pipe"

  it should "turn visit summaries into document visit analytics" in {
    val stream = Stream(
      VisitSummary(VisitCreate(first, first, first, earliestDate), VisitUpdate(first, 2, 1.0, latestDate)),
      VisitSummary(VisitCreate(second, second, second, earliestDate), VisitUpdate(second, 2, 0.5, laterDate)),
      VisitSummary(VisitCreate(third, first, first, latestDate.plusHours(1))),
      VisitSummary(VisitCreate(fourth, first, first, latestDate.plusHours(3)))
    )

    stream.through(VisitAnalytics.toDocumentVisitAnalytics).toList should contain theSameElementsAs
      DocumentVisitAnalytics.fromSummaries(
        List(
          VisitSummary(VisitCreate(first, first, first, earliestDate), VisitUpdate(first, 2, 1.0, latestDate)),
          VisitSummary(VisitCreate(second, second, second, earliestDate), VisitUpdate(second, 2, 0.5, laterDate)),
          VisitSummary(VisitCreate(third, first, first, latestDate.plusHours(1))),
          VisitSummary(VisitCreate(fourth, first, first, latestDate.plusHours(3)))
        )
      )
  }
}
