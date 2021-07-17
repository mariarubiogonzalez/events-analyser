package mariarubiogonzalez.analyser

import akka.actor.ActorSystem
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes.OK
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import spray.json.DefaultJsonProtocol._
import spray.json.RootJsonFormat

case class Routes(state: State)(implicit val system: ActorSystem) extends SprayJsonSupport {

  val metrics: Route =
    get {
      path("metrics") {
        complete(
          OK,
          Metrics(from = 1L, to = 1L, count = state.get)
        )
      }
    }
}

case class Metrics(from: Long, to: Long, count: Map[String, Map[String, Int]])
object Metrics {
  implicit val jsonFormat: RootJsonFormat[Metrics] = jsonFormat3(Metrics.apply)
}
