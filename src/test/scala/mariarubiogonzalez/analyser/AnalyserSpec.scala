package mariarubiogonzalez.analyser

import akka.actor.ActorSystem
import akka.stream.scaladsl.{Sink, StreamConverters}
import org.scalactic.TypeCheckedTripleEquals
import org.scalatest.BeforeAndAfterAll
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets.UTF_8
import scala.concurrent.duration._

class AnalyserSpec
    extends AnyFreeSpec
    with Matchers
    with TypeCheckedTripleEquals
    with ScalaFutures
    with BeforeAndAfterAll {

  implicit val actorSystem: ActorSystem                = ActorSystem(s"${this.getClass.getSimpleName}-actor-system")
  override implicit val patienceConfig: PatienceConfig = PatienceConfig(1.seconds, 50.milliseconds)

  override protected def afterAll(): Unit = {
    super.afterAll()
    actorSystem.terminate().futureValue
  }

  "should analyse events to calculate windowed word count by event type" in {

    val events = Seq(
      """{ "event_type": "baz", "data": "amet", "timestamp": 1626558031 }""",
      """{ "event_type": "foo", "data": "lorem", "timestamp": 1626558037 }""",
      """{ "event_type": "foo", "data": "lorem", "timestamp": 1626558037 }""",
      """{ "event_type": "bar", "data": "dolor", "timestamp": 1626558040 }""",
      """{ "event_type": "bar", "data": "sit", "timestamp": 1626558040 }""",
      """{ "event_type": "foo", "data": "dolor", "timestamp": 1626558042 }"""
    ).mkString("\n").getBytes(UTF_8.name)

    val state = new State()

    val analyser = Analyser(
      source = StreamConverters.fromInputStream { () => new ByteArrayInputStream(events) },
      state = state
    )
    analyser.stream.runWith(Sink.ignore).futureValue

    state.get should ===(
      Map(
        "baz" -> Map(
          "amet" -> 1
        ),
        "foo" -> Map(
          "lorem" -> 2,
          "dolor" -> 1
        ),
        "bar" -> Map(
          "dolor" -> 1,
          "sit"   -> 1
        )
      )
    )
  }
}
