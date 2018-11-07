package com.spikerlabs.streamingapp.domain

import java.time.ZonedDateTime
import java.util.UUID

import io.circe.{Decoder, HCursor}
import io.circe.generic.semiauto._


trait Message {
  def id: UUID
}

object Message {
  import io.circe.java8.time.decodeZonedDateTime

  case class VisitCreate(id: UUID, userId: UUID, documentId: UUID, createdAt: ZonedDateTime) extends Message
  object VisitCreate {
    implicit val decoder = deriveDecoder[VisitCreate]
  }

  case class VisitUpdate(id: UUID, engagedTime: Int, completion: Double, updatedAt: ZonedDateTime) extends Message
  object VisitUpdate {
    implicit val decoder = deriveDecoder[VisitUpdate]
  }

  implicit val messageDecoder: Decoder[Message] = new Decoder[Message] {
    final def apply(c: HCursor): Decoder.Result[Message] =
      for {
        messageType <- c.downField("messageType").as[String]
      } yield {
        (messageType match {
          case "VisitCreate" => c.downField("visit").as[VisitCreate]
          case "VisitUpdate" => c.downField("visit").as[VisitUpdate]
        }).getOrElse(throw new Exception(s"could not parse the $messageType"))
      }
  }

}
