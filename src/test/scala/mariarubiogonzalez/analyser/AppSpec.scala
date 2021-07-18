package mariarubiogonzalez.analyser
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding.Get
import akka.http.scaladsl.model.StatusCodes.OK
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.scaladsl.StreamConverters
import org.scalactic.TypeCheckedTripleEquals
import org.scalatest.BeforeAndAfterAll
import org.scalatest.concurrent.{Eventually, IntegrationPatience, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import spray.json._

import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets.UTF_8

class AppSpec
    extends AnyFreeSpec
    with Matchers
    with TypeCheckedTripleEquals
    with ScalaFutures
    with Eventually
    with BeforeAndAfterAll
    with IntegrationPatience {

  private implicit val system: ActorSystem = ActorSystem(s"${this.getClass.getSimpleName}-actor-system")

  private val events = Seq(
    """{ "event_type": "foo", "data": "lorem", "timestamp": 1626558037 }""",
    """{ "event_type": "foo", "data": "ipsum", "timestamp": 1626558040 }""",
    """{ "event_type": "bar", "data": "lorem", "timestamp": 1626558040 }"""
  ).mkString("\n").getBytes(UTF_8.name)

  private val app = new App(source = StreamConverters.fromInputStream { () => new ByteArrayInputStream(events) })

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    app.start()
  }

  override protected def afterAll(): Unit = {
    super.afterAll()
    app.stop().futureValue
    system.terminate().futureValue
  }

  "App analyses input events and serves windowed word count metrics" in {

    eventually {
      val response = Http().singleRequest(Get(s"http://localhost:8080/metrics")).futureValue
      response.status should ===(OK)
      val metrics: Metrics = Unmarshal(response.entity).to[String].futureValue.parseJson.convertTo[Metrics]
      metrics should ===(
        Metrics(
          from = 1626558031,
          to = 1626558040,
          metrics = Seq(
            Metric("bar", Seq(WordCount("lorem", 1))),
            Metric("foo", Seq(WordCount("ipsum", 1), WordCount("lorem", 1)))
          )
        )
      )
    }
  }

}
