package com.spikerlabs.streamingapp.domain

import java.nio.file.{Files, Paths}
import java.util.concurrent.Executors

import com.spikerlabs.streamingapp.domain.Message.VisitCreate
import org.scalatest.{AppendedClues, FlatSpec, Matchers}

import scala.concurrent.{ExecutionContext, ExecutionContextExecutorService}

class MessageStreamSpec extends FlatSpec with Matchers with AppendedClues {

  implicit val blockExecutionService = ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(2))

  behavior of "message stream factory"

  it should "produce an empty stream for an empty file" in {
    val path = Paths.get("/tmp/streaming-app-example-message-stream-spec-empty.data")
    Files.write(path, "".getBytes())

    val data = MessageStream.fromFile(path).compile.toVector.unsafeRunSync()

    data shouldBe empty
  }

  it should "produce a stream with two visit create messages" in {
    val path = Paths.get("/tmp/streaming-app-example-message-stream-spec-visit-create.data")
    Files.write(path,
      """
        |{"messageType": "VisitCreate","visit": {"id": "6f200a5d-628c-4311-aec9-50b8cb481bf5","userId": "c9c5f9a7-cc2f-47a5-b089-06e1b7aef129","documentId": "62e09c7d-714d-40a6-9e6e-fdc525a90d59","createdAt": "2015-01-01T11:22:33.000Z"}}
        |{"messageType": "VisitCreate","visit": {"id": "c63bd06d-dec0-465f-bba5-a2b61f438275","userId": "20879fe7-a946-4ca1-b0f6-f8f10e753fba","documentId": "a8e5010b-aa0a-44c3-b8b2-6865ca0bac90","createdAt": "2015-02-02T22:33:44.000Z"}}
      """.stripMargin.getBytes())

    val data = MessageStream.fromFile(path).compile.toVector.unsafeRunSync()

    data should have size 2
    data.foreach { message =>
      message shouldBe a[VisitCreate]
    }
  }

}
