package mariarubiogonzalez.analyser

import akka.http.scaladsl.model._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalactic.TypeCheckedTripleEquals
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class RoutesSpec
    extends AnyFreeSpec
    with Matchers
    with ScalaFutures
    with ScalatestRouteTest
    with TypeCheckedTripleEquals {

  private val state  = new State()
  private val routes = Routes(state)

  "Metrics route (GET /metrics) should return latest windowed word count by event type" in {
    val request = Get(uri = "/metrics")
    request ~> routes.metrics ~> check {
      status should ===(StatusCodes.OK)
    }
  }
}
