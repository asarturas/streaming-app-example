package com.spikerlabs.streamingapp.domain.message

import java.time.ZonedDateTime
import java.util.UUID

import io.circe.parser._
import org.scalatest.{AppendedClues, FlatSpec, Matchers}

class VisitUpdateSpec extends FlatSpec with Matchers with AppendedClues {

  behavior of "json decoder"

  it should "decode raw json" in {
    val rawJson = """
        |{
        |  "id": "82abce83-3892-48ee-9f1b-d34c4746ace7",
        |  "engagedTime": 25,
        |  "completion": 0.4,
        |  "updatedAt": "2015-04-22T11:42:35.122Z"
        |}
      """.stripMargin
    val visit = decode[VisitUpdate](rawJson)
    visit.isRight shouldBe true withClue visit
    visit.right.get shouldBe VisitUpdate(
      id = UUID.fromString("82abce83-3892-48ee-9f1b-d34c4746ace7"),
      engagedTime = 25,
      completion = 0.4,
      updatedAt = ZonedDateTime.parse("2015-04-22T11:42:35.122Z")
    )
  }

}
