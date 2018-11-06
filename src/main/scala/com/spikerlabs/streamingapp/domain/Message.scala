package com.spikerlabs.streamingapp.domain

import java.time.LocalDateTime
import java.util.UUID

trait Message {
  def id: UUID
}

object Message {
  case class VisitCreate(id: UUID, userId: UUID, documentId: UUID, createdAt: LocalDateTime) extends Message
  case class VisitUpdate(id: UUID, engagedTime: Int, completion: Double, updatedAt: LocalDateTime) extends Message
}
