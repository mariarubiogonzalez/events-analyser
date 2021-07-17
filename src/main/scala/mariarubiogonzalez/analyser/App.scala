package mariarubiogonzalez.analyser

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.{Failure, Success}

object App {

  private def start(routes: Route)(implicit system: ActorSystem): Future[Http.ServerBinding] = {
    implicit val executionContext: ExecutionContextExecutor = system.getDispatcher

    val futureBinding = Http().newServerAt("localhost", 8080).bind(routes)
    futureBinding.onComplete {
      case Success(binding) =>
        val address = binding.localAddress
        system.log.info("Server online at http://{}:{}/", address.getHostString, address.getPort)
      case Failure(ex) =>
        system.log.error("Failed to bind HTTP endpoint, terminating system", ex)
        system.terminate()
    }
    futureBinding
  }

  def main(args: Array[String]): Unit = {
    implicit val actorSytem: ActorSystem = ActorSystem("events-analyser-actor-system")

    val routes = new Routes()
    start(routes.metrics)
  }

}
