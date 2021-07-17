package mariarubiogonzalez.analyser
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding.Get
import akka.http.scaladsl.model.StatusCodes.OK
import akka.http.scaladsl.unmarshalling.Unmarshal
import org.scalactic.TypeCheckedTripleEquals
import org.scalatest.BeforeAndAfterAll
import org.scalatest.concurrent.{Eventually, IntegrationPatience, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import spray.json._

class AppSpec
    extends AnyFreeSpec
    with Matchers
    with TypeCheckedTripleEquals
    with ScalaFutures
    with Eventually
    with BeforeAndAfterAll
    with IntegrationPatience {

  private implicit val system: ActorSystem = ActorSystem("AppSpec-actor-system")

  private val app = new App()

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
      metrics.count should not be empty
    }
  }

}
