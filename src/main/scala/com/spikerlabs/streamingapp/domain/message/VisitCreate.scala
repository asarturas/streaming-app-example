package com.spikerlabs.streamingapp.domain.message

import java.time.ZonedDateTime
import java.util.UUID

import com.spikerlabs.streamingapp.domain.Message

case class VisitCreate(id: UUID, userId: UUID, documentId: UUID, createdAt: ZonedDateTime) extends Message
object VisitCreate {
  import io.circe.generic.semiauto.deriveDecoder
  import io.circe.java8.time.decodeZonedDateTime
  implicit val decoder = deriveDecoder[VisitCreate]
}