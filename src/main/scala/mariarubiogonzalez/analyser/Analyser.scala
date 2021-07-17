package mariarubiogonzalez.analyser
import akka.Done
import akka.stream.scaladsl.{Framing, Source}
import akka.stream.{ActorAttributes, IOResult, Materializer, Supervision}
import akka.util.ByteString
import cats.implicits._
import spray.json.DefaultJsonProtocol._
import spray.json._

import scala.concurrent.Future

case class Analyser(source: Source[akka.util.ByteString, Future[IOResult]], state: State)(implicit
    val materializer: Materializer
) {
  val stream: Source[Done, Future[IOResult]] = source
    .via(Framing.delimiter(ByteString("\n"), maximumFrameLength = 256, allowTruncation = true))
    .map(_.utf8String)
    .map(_.parseJson.convertTo[Event])
    .map(event => state.put(event.event_type, event.data))
    .withAttributes(ActorAttributes.supervisionStrategy({ _ => Supervision.Resume }))
}

case class Event(event_type: String, data: String, timestamp: Long)
object Event {
  implicit val format: RootJsonFormat[Event] = jsonFormat3(Event.apply)
}

class State {

  private var state: Map[String, Map[String, Int]] = Map()

  def get: Map[String, Map[String, Int]] = state

  def put(eventType: String, word: String): Done = {
    state = state |+| Map(eventType -> Map(word -> 1))
    Done
  }

}
