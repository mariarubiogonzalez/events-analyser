package mariarubiogonzalez.analyser
import akka.Done
import akka.stream.scaladsl.{Framing, Source}
import akka.stream.{ActorAttributes, IOResult, Materializer, Supervision}
import akka.util.ByteString
import cats.implicits._
import spray.json.DefaultJsonProtocol._
import spray.json._

import scala.collection.immutable.SortedMap
import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration

case class Analyser(source: Source[akka.util.ByteString, Future[IOResult]], state: State, window: FiniteDuration)(
    implicit val materializer: Materializer
) {
  val stream: Source[Done, Future[IOResult]] = source
    .via(Framing.delimiter(ByteString("\n"), maximumFrameLength = 256, allowTruncation = true))
    .map(_.utf8String)
    .map(_.parseJson.convertTo[Event])
    .statefulMapConcat { () =>
      var latestSeenTimestamp: Long = Long.MinValue

      { event =>
        latestSeenTimestamp = scala.math.max(latestSeenTimestamp, event.timestamp)
        state.updateWith { currentState: SortedMap[Long, Map[String, Map[String, Int]]] =>
          (currentState |+| SortedMap(event.timestamp -> Map(event.event_type -> Map(event.data -> 1))))
            .rangeFrom(latestSeenTimestamp - window.toSeconds + 1)
        }
        Seq(Done)
      }
    }
    .withAttributes(ActorAttributes.supervisionStrategy({ _ => Supervision.Resume }))
}

case class Event(event_type: String, data: String, timestamp: Long)
object Event {
  implicit val format: RootJsonFormat[Event] = jsonFormat3(Event.apply)
}
