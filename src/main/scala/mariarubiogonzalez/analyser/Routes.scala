package mariarubiogonzalez.analyser

import akka.actor.ActorSystem
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes.OK
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import spray.json.DefaultJsonProtocol._
import spray.json.RootJsonFormat

import java.time.Instant
import scala.concurrent.duration.FiniteDuration

case class Routes(state: State, window: FiniteDuration, now: () => Instant = Instant.now)(implicit
    val system: ActorSystem
) extends SprayJsonSupport {

  val metrics: Route =
    get {
      path("metrics") {
        val (latestTimestampSeen, count) = state.get
        val to                           = latestTimestampSeen.getOrElse(now().getEpochSecond)
        complete(
          OK,
          Metrics(
            from = to - window.toSeconds + 1,
            to = to,
            count = count
          )
        )
      }
    }
}

case class Metrics(from: Long, to: Long, count: Map[String, Map[String, Int]])
object Metrics {
  implicit val jsonFormat: RootJsonFormat[Metrics] = jsonFormat3(Metrics.apply)
}
