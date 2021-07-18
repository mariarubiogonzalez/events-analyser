package mariarubiogonzalez.analyser

import akka.http.scaladsl.model._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalactic.TypeCheckedTripleEquals
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import spray.json._

import java.time.Instant
import scala.concurrent.duration._
import scala.collection.immutable.SortedMap

class RoutesSpec
    extends AnyFreeSpec
    with Matchers
    with ScalaFutures
    with ScalatestRouteTest
    with TypeCheckedTripleEquals {

  private val window = 10.seconds

  "Metrics route (GET /metrics) should return latest windowed word count by event type" in {
    val state = State(
      SortedMap(
        1626558037L -> Map("foo" -> Map("ipse" -> 2)),
        1626558038L -> Map("foo" -> Map("ipse" -> 2)),
        1626558039L -> Map("foo" -> Map("ipse" -> 2))
      )
    )
    val routes  = Routes(state, window)
    val request = Get(uri = "/metrics")
    request ~> routes.metrics ~> check {
      status should ===(StatusCodes.OK)
      responseAs[String].parseJson.convertTo[Metrics] should ===(
        Metrics(
          from = 1626558039L - window.toSeconds + 1,
          to = 1626558039L,
          metrics = Seq(
            Metric("foo", Seq(WordCount("ipse", 6)))
          )
        )
      )
    }
  }

  "Metrics route (GET /metrics) should return empty metrics and default the window to the current time" in {
    val now     = Instant.now
    val routes  = Routes(State(), window, () => now)
    val request = Get(uri = "/metrics")
    request ~> routes.metrics ~> check {
      status should ===(StatusCodes.OK)
      responseAs[String].parseJson.convertTo[Metrics] should ===(
        Metrics(
          from = now.getEpochSecond - window.toSeconds + 1,
          to = now.getEpochSecond,
          metrics = Seq()
        )
      )
    }
  }
}
