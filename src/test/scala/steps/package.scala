import cats.effect.IO
import com.spikerlabs.streamingapp.domain.MessageStream.MessageStream
import fs2.Stream

package object steps {
  case class SharedState(stream: MessageStream[IO])
  var sharedState = SharedState(Stream.empty)
}
