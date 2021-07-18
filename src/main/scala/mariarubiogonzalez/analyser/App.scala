package mariarubiogonzalez.analyser

import akka.actor.{ActorSystem, Terminated}
import akka.http.scaladsl.Http
import akka.stream.IOResult
import akka.stream.scaladsl.{Sink, Source, StreamConverters}
import akka.util.ByteString
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContextExecutor, Future}
import scala.util.{Failure, Success}

class App(
    source: Source[akka.util.ByteString, Future[IOResult]],
    window: FiniteDuration = 10.seconds
) extends LazyLogging {

  implicit val system: ActorSystem          = ActorSystem("events-analyser-actor-system")
  implicit val ec: ExecutionContextExecutor = system.getDispatcher

  def start(): Unit = {
    val state         = State()
    val analyser      = Analyser(source, state, window)
    val routes        = Routes(state, window)
    val futureBinding = Http().newServerAt("localhost", 8080).bind(routes.metrics)
    futureBinding.onComplete {
      case Success(binding) =>
        val address = binding.localAddress
        logger.info("Server online at http://{}:{}/", address.getHostString, address.getPort)
        logger.info(s"Starting events analyser with window of ${window.toSeconds} seconds")
        analyser.stream.runWith(Sink.ignore)
      case Failure(ex) =>
        logger.error("Failed to bind HTTP endpoint, terminating system", ex)
        system.terminate()
    }
  }

  def stop(): Future[Terminated] = {
    system.terminate()
    system.whenTerminated
  }
}

object App {

  def main(args: Array[String]): Unit = {
    val windowInSeconds: Int                              = args.headOption.flatMap(_.toIntOption).getOrElse(10)
    val stdinSource: Source[ByteString, Future[IOResult]] = StreamConverters.fromInputStream(() => System.in)
    val app                                               = new App(stdinSource, windowInSeconds.seconds)
    app.start()

    scala.sys.addShutdownHook {
      Await.result(app.stop(), 30.seconds)
    }
  }

}
