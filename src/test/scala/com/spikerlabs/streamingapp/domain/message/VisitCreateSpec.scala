package com.spikerlabs.streamingapp.domain.message

import java.time.ZonedDateTime
import java.util.UUID

import org.scalatest.{AppendedClues, FlatSpec, Matchers}
import io.circe.parser._

class VisitCreateSpec extends FlatSpec with Matchers with AppendedClues {

  behavior of "json decoder"

  it should "decode raw json" in {
    val rawJson = """
        |{
        |  "id": "82abce83-3892-48ee-9f1b-d34c4746ace7",
        |  "userId": "dc0ad841-0b89-4411-a033-d3f174e8d0ad",
        |  "documentId": "7b2bc74e-f529-4f5d-885b-4377c424211d",
        |  "createdAt": "2015-04-22T11:42:07.602Z"
        |}
      """.stripMargin
    val visit = decode[VisitCreate](rawJson)
    visit.isRight shouldBe true withClue visit
    visit.right.get shouldBe VisitCreate(
      id = UUID.fromString("82abce83-3892-48ee-9f1b-d34c4746ace7"),
      userId = UUID.fromString("dc0ad841-0b89-4411-a033-d3f174e8d0ad"),
      documentId = UUID.fromString("7b2bc74e-f529-4f5d-885b-4377c424211d"),
      createdAt = ZonedDateTime.parse("2015-04-22T11:42:07.602Z")
    )
  }

}
