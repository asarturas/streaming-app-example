package com.spikerlabs.streamingapp.analysis

import com.spikerlabs.streamingapp.domain.Message
import fs2.Pipe

object DocumentVisitAnalytics {
  def aggregateVisits[F[_]]: Pipe[F, Message, Message] = in => in
}
