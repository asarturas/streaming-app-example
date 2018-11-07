package com.spikerlabs.streamingapp.domain.message

import java.time.ZonedDateTime
import java.util.UUID

import com.spikerlabs.streamingapp.domain.Message

case class VisitUpdate(id: UUID, engagedTime: Int, completion: Double, updatedAt: ZonedDateTime) extends Message
  object VisitUpdate {
    import io.circe.generic.semiauto.deriveDecoder
    import io.circe.java8.time.decodeZonedDateTime
    implicit val decoder = deriveDecoder[VisitUpdate]
  }
