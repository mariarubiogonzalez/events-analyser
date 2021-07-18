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
import cats.implicits._

case class Routes(state: State, window: FiniteDuration, now: () => Instant = Instant.now)(implicit
    val system: ActorSystem
) extends SprayJsonSupport {

  val metrics: Route =
    get {
      path("metrics") {
        val latestState = state.get
        val to          = latestState.keys.maxOption.getOrElse(now().getEpochSecond)
        val metrics = latestState.values.flatten
          .groupMapReduce { case (eventType, _) => eventType } { case (_, count) => count } { _ |+| _ }
          .toSeq
          .map { case (eventType, wordCounts) =>
            Metric(eventType, wordCounts.map { case (word, count) => WordCount(word, count) }.toSeq)
          }

        complete(
          OK,
          Metrics(
            from = to - window.toSeconds + 1,
            to = to,
            metrics = metrics
          )
        )
      }
    }
}

case class WordCount(word: String, count: Int)
case class Metric(eventType: String, wordCounts: Seq[WordCount])
case class Metrics(from: Long, to: Long, metrics: Seq[Metric])

object WordCount {
  implicit val jsonFormat: RootJsonFormat[WordCount] = jsonFormat2(WordCount.apply)
}

object Metric {
  implicit val jsonFormat: RootJsonFormat[Metric] = jsonFormat2(Metric.apply)
}

object Metrics {
  implicit val jsonFormat: RootJsonFormat[Metrics] = jsonFormat3(Metrics.apply)
}
