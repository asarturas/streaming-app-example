package com.spikerlabs.streamingapp.transport

import java.nio.file.{Files, Paths}
import java.time.ZonedDateTime
import java.util.UUID
import java.util.concurrent.Executors

import cats.effect.{ContextShift, IO}
import com.spikerlabs.streamingapp.domain.TimePeriod
import com.spikerlabs.streamingapp.domain.message.DocumentVisitAnalytics
import fs2.Stream
import org.scalatest.{AppendedClues, FlatSpec, Matchers}

import scala.concurrent.ExecutionContext

class MessageOutputSpec extends FlatSpec with Matchers with AppendedClues {

  implicit val blockExecutionService = ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(2))
  implicit val cs = IO.contextShift(ExecutionContext.global)

  behavior of "file output"

  it should "store a stream of analytics messages to a file" in {
    val path = Paths.get("/tmp/streaming-app-example-message-stream-spec-visit-create-result.data")
    val stream = Stream.emits(List(
      DocumentVisitAnalytics(
        UUID.fromString("6f200a5d-628c-4311-aec9-50b8cb481bf5"),
        TimePeriod(ZonedDateTime.parse("2015-01-01T00:00Z"), ZonedDateTime.parse("2015-01-01T01:00Z")),
        visits = 4,
        uniques = 3,
        time = 2,
        completion = 1
      ),
      DocumentVisitAnalytics(
        UUID.fromString("c9c5f9a7-cc2f-47a5-b089-06e1b7aef129"),
        TimePeriod(ZonedDateTime.parse("2015-01-01T01:00Z"), ZonedDateTime.parse("2015-01-01T02:00Z")),
        visits = 5,
        uniques = 4,
        time = 3,
        completion = 2
      )
    ))

    stream.through(MessageOutput.writeToFile[IO](path)).compile.toVector.unsafeRunSync()

    Files.readAllLines(path) should contain allElementsOf List(
      "6f200a5d-628c-4311-aec9-50b8cb481bf5|2015-01-01T00:00Z|2015-01-01T01:00Z|4|3|2.0|1",
      "c9c5f9a7-cc2f-47a5-b089-06e1b7aef129|2015-01-01T01:00Z|2015-01-01T02:00Z|5|4|3.0|2",
    )

  }

}
