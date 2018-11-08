package com.spikerlabs.streamingapp.analysis

import java.time.ZonedDateTime
import java.util.UUID

import com.spikerlabs.streamingapp.domain.message.{VisitCreate, VisitUpdate}
import org.scalatest.{AppendedClues, FlatSpec, Matchers}
import fs2.{Chunk, Pure, Stream}

class DocumentVisitAnalyticsSpec extends FlatSpec with Matchers with AppendedClues {

  val first = UUID.nameUUIDFromBytes("1".getBytes())
  val second = UUID.nameUUIDFromBytes("2".getBytes())
  val third = UUID.nameUUIDFromBytes("3".getBytes())
  val earliestDate = ZonedDateTime.parse("2015-01-01T00:00Z")
  val earlierDate = ZonedDateTime.parse("2015-01-01T00:01Z")
  val laterDate = ZonedDateTime.parse("2015-01-01T00:02Z")
  val latestDate = ZonedDateTime.parse("2015-01-01T00:03Z")

  behavior of "order the stream by visit date"

  it should "not reorder already ordered stream" in {
    val stream = Stream(
      VisitCreate(first, first, first, earliestDate),
      VisitUpdate(first, 1, 1, earlierDate),
      VisitCreate(second, second, second, laterDate),
      VisitUpdate(second, 1, 1, latestDate),
    )

    stream.through(DocumentVisitAnalytics.orderByVisitDate()).toList should
      contain theSameElementsInOrderAs stream.toList
  }

  it should "reorder items out of order, when they are within the threshold" in {
    val stream = Stream(
      VisitUpdate(first, 1, 1, earlierDate),
      VisitCreate(first, first, first, earliestDate),
      VisitUpdate(second, 1, 1, latestDate),
      VisitCreate(second, second, second, laterDate),
    )

    stream.through(DocumentVisitAnalytics.orderByVisitDate(2)).toList should contain theSameElementsInOrderAs
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

    stream.through(DocumentVisitAnalytics.orderByVisitDate(2)).toList should contain theSameElementsInOrderAs
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

    stream.through(DocumentVisitAnalytics.orderByVisitDate(2, 3)).toList should contain theSameElementsInOrderAs
      List(
        VisitUpdate(first, 1, 1, earlierDate), // this is released when buffer size reaches the threshold when laterDate message arrives
        VisitCreate(first, first, first, earliestDate),
        VisitCreate(second, second, second, laterDate),
        VisitUpdate(second, 1, 1, latestDate),
      )
  }

}
