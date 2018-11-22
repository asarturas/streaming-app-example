package steps

import java.io.InputStream
import java.nio.file.Paths
import java.util.concurrent.Executors

import cucumber.api.scala.{EN, ScalaDsl}
import cucumber.api.PendingException
import org.scalatest.{AppendedClues, Matchers}

class BuildContextSteps extends ScalaDsl with EN with Matchers with AppendedClues {

  import BuildContextSteps._

  When("""^I build the container locally$""") { () =>
    runCommand("sbt", "docker:stage") shouldBe 0
    runCommand("sbt", "docker:publishLocal") shouldBe 0
  }
  Then("""^I should have container "([^"]*)" available in my system docker installation$"""){ (containerName: String) =>
    runCommand("docker", "inspect", "--type=image", containerName) shouldBe 0
  }

}

object BuildContextSteps {

  import java.io.BufferedReader
  import java.io.InputStreamReader
  import java.util.function.Consumer

  def runCommand(command: String*): Int = {
    val processBuilder = new ProcessBuilder()
    println(command.mkString(" "))
    processBuilder.command(command: _*)
    processBuilder.directory(Paths.get(".").toFile.getCanonicalFile)

    val process = processBuilder.start()
    Executors.newSingleThreadExecutor.submit(new Process(process.getInputStream, s => println(s)))
    val exitCode = process.waitFor
    exitCode
  }

  private class Process(var inputStream: InputStream, var consumerF: String => Unit) extends Runnable {

    import scala.compat.java8.FunctionConverters._

    val consumer = asJavaConsumer[String](consumerF)

    override def run(): Unit = {
      new BufferedReader(new InputStreamReader(inputStream)).lines.forEach(consumer)
    }
  }

}