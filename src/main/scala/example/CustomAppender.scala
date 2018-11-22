package example

import java.util.concurrent.Executors

import cats.effect.{Concurrent, ContextShift, Effect, IO}
import fs2.concurrent.{Queue, Topic}
import fs2.Stream
import org.apache.logging.log4j.core.{Filter, LogEvent}
import org.apache.logging.log4j.core.appender.AbstractAppender
import org.apache.logging.log4j.core.config.plugins._

import scala.concurrent.{ExecutionContext, ExecutionContextExecutorService}

@Plugin(name = "CustomAppender", category = "Core", elementType = "appender")
class CustomAppender(name: String, filter: Filter, queue: Queue[IO, LogEvent]) extends AbstractAppender(name, filter, null) {

  override def append(event: LogEvent): Unit = {
    println("burokas0")
    queue.enqueue1(event).unsafeRunAsyncAndForget()
    println("burokas1")
  }

}

object CustomAppender {

  implicit val ec = scala.concurrent.ExecutionContext.Implicits.global
  implicit val ioContextShift: ContextShift[IO] = IO.contextShift(ec)

  def sharedTopicStream[F[_] : Effect : Concurrent](topicId: String)(implicit ec: ExecutionContext): Stream[F, Topic[F, String]] =
      Stream.eval(Topic[F, String](s"Topic $topicId start"))

  val queue = Queue.circularBuffer[IO, LogEvent](100).unsafeRunSync()
  val topic = sharedTopicStream[IO]("sharedTopic")
//  val topic

  @PluginFactory
  def createAppender(
                      @PluginAttribute("name") name: String,
                      @PluginElement("filter") filter: Filter
                    ) = {
    val c = new CustomAppender(name, filter, queue)
    run
    c
  }
  def run = {
    var appender = null
    import scala.concurrent.JavaConversions._
    val stream = for {
      _ <- queue.dequeue
      .through(_.evalMap{e => IO { println(e.getContextData.toMap); println("burokas2"); e}})
      .onComplete {
        println("burokas end")
        Stream.empty
      }
//
    } yield ()
    stream.compile.drain.unsafeRunAsyncAndForget()
    println("burokas start")
    Thread.sleep(1000)
//    queue.unsafeRunSync().dequeue
//      .through(_.evalMap{e => IO { println(e.toString); e}})
//      .onComplete {
//        println("end")
//        Stream.empty
//      }
//      .compile.drain.unsafeRunAsyncAndForget()
  }


}