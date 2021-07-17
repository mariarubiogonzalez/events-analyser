package mariarubiogonzalez.analyser
import akka.Done

import scala.collection.immutable.SortedMap
import cats.implicits._

case class State(initialState: SortedMap[Long, Map[String, Map[String, Int]]] = SortedMap()) {

  private var state: SortedMap[Long, Map[String, Map[String, Int]]] = initialState

  def get: (Option[Long], Map[String, Map[String, Int]]) = {
    val s = state
    val wordCountByEventType = s.values.flatten
      .groupMapReduce { case (eventType, _) => eventType } { case (_, count) => count } { _ |+| _ }
    val latestTimestampSeen = s.keys.maxOption
    (latestTimestampSeen, wordCountByEventType)
  }

  def updateWith(
      fn: SortedMap[Long, Map[String, Map[String, Int]]] => SortedMap[Long, Map[String, Map[String, Int]]]
  ): Done = {
    state = fn(state)
    Done
  }

}
