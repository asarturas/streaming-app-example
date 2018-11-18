package com.spikerlabs.streamingapp.transport

import java.nio.file.{Files, Paths}
import java.time.ZonedDateTime
import java.util.concurrent.Executors
import java.util.UUID

import cats.effect.{ContextShift, IO}
import fs2.Stream
import com.spikerlabs.streamingapp.domain.message.{DocumentVisitAnalytics, VisitCreate, VisitUpdate}
import com.spikerlabs.streamingapp.domain.TimePeriod
import org.scalatest.{AppendedClues, FlatSpec, Matchers}

import scala.concurrent.ExecutionContext

class MessageInputSpec extends FlatSpec with Matchers with AppendedClues {

  implicit val blockExecutionService = ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(2))
  implicit val ioContextShift: ContextShift[IO] = IO.contextShift(blockExecutionService)

  behavior of "file input"

  it should "produce an empty stream for an empty file" in {
    val path = Paths.get("/tmp/streaming-app-example-message-stream-spec-empty.data")
    Files.write(path, "".getBytes())

    val data = MessageInput.fromFile(path).compile.toVector.unsafeRunSync()

    data shouldBe empty
  }

  it should "produce a stream with valid messages" in {
    val path = Paths.get("/tmp/streaming-app-example-message-stream-spec-visit-create.data")
    Files.write(path,
      """
        |{"messageType": "VisitCreate","visit": {"id": "6f200a5d-628c-4311-aec9-50b8cb481bf5","userId": "c9c5f9a7-cc2f-47a5-b089-06e1b7aef129","documentId": "62e09c7d-714d-40a6-9e6e-fdc525a90d59","createdAt": "2015-01-01T11:22:33.000Z"}}
        |{"messageType": "VisitUpdate","visit": {"id": "6f200a5d-628c-4311-aec9-50b8cb481bf5","engagedTime": 25,"completion": 0.4,"updatedAt": "2015-01-01T11:33:44.999Z"}}
      """.stripMargin.getBytes())

    val data = MessageInput.fromFile(path).compile.toVector.unsafeRunSync()

    data shouldBe Vector(
      VisitCreate(
        id = UUID.fromString("6f200a5d-628c-4311-aec9-50b8cb481bf5"),
        userId = UUID.fromString("c9c5f9a7-cc2f-47a5-b089-06e1b7aef129"),
        documentId = UUID.fromString("62e09c7d-714d-40a6-9e6e-fdc525a90d59"),
        createdAt = ZonedDateTime.parse("2015-01-01T11:22:33.000Z")
      ),
      VisitUpdate(
        id = UUID.fromString("6f200a5d-628c-4311-aec9-50b8cb481bf5"),
        engagedTime = 25,
        completion = 0.4,
        updatedAt = ZonedDateTime.parse("2015-01-01T11:33:44.999Z")
      )
    )
  }

}
