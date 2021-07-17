package mariarubiogonzalez.analyser
import akka.Done
import akka.stream.{IOResult, Materializer}
import akka.stream.scaladsl.{Framing, Sink, Source}
import akka.util.ByteString
import spray.json._
import DefaultJsonProtocol._

import scala.concurrent.Future
import cats.implicits._

case class Analyser(source: Source[akka.util.ByteString, Future[IOResult]], state: State)(implicit
    val materializer: Materializer
) {
  val stream: Source[Done, Future[IOResult]] = source
    .via(Framing.delimiter(ByteString("\n"), maximumFrameLength = 256, allowTruncation = true))
    .map(_.utf8String)
    .map(_.parseJson.convertTo[Event])
    .map(event => state.put(event.event_type, event.data))
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
