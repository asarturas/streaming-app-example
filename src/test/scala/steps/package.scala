import java.util.concurrent.Executors

import cats.effect.IO
import com.spikerlabs.streamingapp.domain.Message
import com.spikerlabs.streamingapp.domain.MessageStream.MessageStream

import scala.concurrent.{ExecutionContext, ExecutionContextExecutorService}

package object steps {

  implicit val blockingContextService: ExecutionContextExecutorService = ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(2))

  case class SharedState(stream: MessageStream, maybeEvaluatedStream: Option[Vector[Message]] = None)

  var sharedState: SharedState = _

  def unsafeEvaluateStream(): Unit = {
    if (sharedState.maybeEvaluatedStream.isEmpty) {
      val evaluatedStream = sharedState.stream.compile.toVector.unsafeRunSync()
      sharedState = sharedState.copy(maybeEvaluatedStream = Some(evaluatedStream))
    }
  }

}
