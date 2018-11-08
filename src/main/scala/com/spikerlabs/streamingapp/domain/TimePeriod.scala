package com.spikerlabs.streamingapp.domain

import java.time.ZonedDateTime

import com.spikerlabs.streamingapp.domain.message.{VisitCreate, VisitUpdate}

case class TimePeriod(startTime: ZonedDateTime, endTime: ZonedDateTime)

object TimePeriod {

  def fromVisitUpdate(message: VisitUpdate): TimePeriod = fromTime(message.updatedAt)

  def fromVisitCreate(message: VisitCreate): TimePeriod = fromTime(message.createdAt)

  private def fromTime(time: ZonedDateTime): TimePeriod = {
    val startTime = ZonedDateTime.of(time.getYear, time.getMonth.getValue, time.getDayOfMonth, time.getHour, 0, 0, 0, time.getZone)
    val endTime = startTime.plusHours(1)
    TimePeriod(startTime, endTime)
  }

}