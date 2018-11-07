package com.spikerlabs.streamingapp.domain.message

import java.time.ZonedDateTime
import java.util.UUID

case class DocumentVisitAnalytics(documentId: UUID, startTime: ZonedDateTime, endTime: ZonedDateTime, visits: Int, uniques: Int, time: Double, completion: Int)