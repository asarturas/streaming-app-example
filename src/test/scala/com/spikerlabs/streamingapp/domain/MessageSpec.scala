package com.spikerlabs.streamingapp.domain

import java.time.ZonedDateTime
import java.util.UUID

import com.spikerlabs.streamingapp.domain.message.{VisitCreate, VisitUpdate}
import org.scalatest.{AppendedClues, FlatSpec, Matchers}
import io.circe.parser._

class MessageSpec extends FlatSpec with Matchers with AppendedClues {

  behavior of "message parser"

  it should "parse visit create json message" in {
    val jsonString =
      """
        |{
        |  "messageType": "VisitCreate",
        |  "visit": {
        |    "id": "82abce83-3892-48ee-9f1b-d34c4746ace7",
        |    "userId": "dc0ad841-0b89-4411-a033-d3f174e8d0ad",
        |    "documentId": "7b2bc74e-f529-4f5d-885b-4377c424211d",
        |    "createdAt": "2015-04-22T11:42:07.602Z"
        |  }
        |}
      """.stripMargin
    val message = decode[Message](jsonString)
    message.isRight shouldBe true withClue message
    message.right.get shouldBe a[VisitCreate]
  }

  it should "parse visit update json message" in {
    val rawJson = """
        |{
        |  "messageType": "VisitUpdate",
        |  "visit": {
        |    "id": "82abce83-3892-48ee-9f1b-d34c4746ace7",
        |    "engagedTime": 25,
        |    "completion": 0.4,
        |    "updatedAt": "2015-04-22T11:42:35.122Z"
        |  }
        |}
      """.stripMargin
    val message = decode[Message](rawJson)
    message.isRight shouldBe true withClue message
    message.right.get shouldBe a[VisitUpdate]
  }

}
