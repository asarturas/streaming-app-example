package com.spikerlabs.streamingapp.domain.message

import com.spikerlabs.streamingapp.domain.Message

case class VisitInProgress(create: VisitCreate, lastUpdate: Option[VisitUpdate] = None) extends Message
