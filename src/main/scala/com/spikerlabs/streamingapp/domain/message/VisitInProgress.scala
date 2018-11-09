package com.spikerlabs.streamingapp.domain.message

case class VisitInProgress(create: VisitCreate, lastUpdate: Option[VisitUpdate] = None)
