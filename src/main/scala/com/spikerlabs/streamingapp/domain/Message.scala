package com.spikerlabs.streamingapp.domain

import java.time.ZonedDateTime
import java.util.UUID

import com.spikerlabs.streamingapp.domain.message.{VisitCreate, VisitUpdate}
import io.circe.{Decoder, HCursor}
import io.circe.generic.semiauto._


trait Message

object Message {

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

  implicit val messageOrdering: Ordering[Message] = Ordering.fromLessThan[Message] { (one, another) =>
    date(one).isBefore(date(another))
  }

  def date(msg: Message): ZonedDateTime = msg match {
    case msg: VisitCreate => msg.createdAt
    case msg: VisitUpdate => msg.updatedAt
  }

}
