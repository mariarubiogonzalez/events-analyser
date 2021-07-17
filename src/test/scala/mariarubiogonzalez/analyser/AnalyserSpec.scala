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
    )

    run(events) should ===(
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

  "should ignore invalid events" in {
    val events = Seq(
      """{ "event_type": "foo", "data": "ipsum", "timestamp": 1626558040 }""",
      """{ "event_type": "bar", "data": "lorem", "timestamp": 1626558040 }""",
      """{ "event_type": "bar", "data": "dolor", "timestamp": 1626558040 }""",
      """{ "event_type": "bar", "data": "sit", "timestamp": 1626558040 }""",
      """{ "e�;""",
      """"  2""",
      """{ "event_type": "bar", "data": "lorem", "timestamp": 1626558040 }"""
    )

    run(events) should ===(
      Map(
        "foo" -> Map(
          "ipsum" -> 1
        ),
        "bar" -> Map(
          "lorem" -> 2,
          "dolor" -> 1,
          "sit"   -> 1
        )
      )
    )

  }

  "should only keep the word count for the latest sliding window" in {
    val events = Seq(
      """{ "event_type": "baz", "data": "amet", "timestamp": 1626558031 }""",
      """{ "event_type": "foo", "data": "lorem", "timestamp": 1626558037 }""",
      """{ "event_type": "foo", "data": "lorem", "timestamp": 1626558037 }""",
      """{ "event_type": "bar", "data": "dolor", "timestamp": 1626558040 }""",
      """{ "event_type": "bar", "data": "sit", "timestamp": 1626558040 }""",
      """{ "event_type": "foo", "data": "dolor", "timestamp": 1626558042 }"""
    )

    run(events, window = 3.seconds) should ===(
      Map(
        "bar" -> Map(
          "dolor" -> 1,
          "sit"   -> 1
        ),
        "foo" -> Map(
          "dolor" -> 1
        )
      )
    )
  }

  private def run(events: Seq[String], window: FiniteDuration = 15.seconds): Map[String, Map[String, Int]] = {
    val state = State()
    val source = StreamConverters.fromInputStream { () =>
      new ByteArrayInputStream(events.mkString("\n").getBytes(UTF_8.name))
    }
    val analyser = Analyser(
      source = source,
      state = state,
      window = window
    )
    analyser.stream.runWith(Sink.ignore).futureValue
    state.get._2
  }

}
