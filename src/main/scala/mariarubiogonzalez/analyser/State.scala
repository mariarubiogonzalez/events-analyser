package mariarubiogonzalez.analyser
import akka.Done

import scala.collection.immutable.SortedMap
import cats.implicits._

case class State(initialState: SortedMap[Long, Map[String, Map[String, Int]]] = SortedMap()) {

  private var state: SortedMap[Long, Map[String, Map[String, Int]]] = initialState

  def get: SortedMap[Long, Map[String, Map[String, Int]]] = state

  def updateWith(
      fn: SortedMap[Long, Map[String, Map[String, Int]]] => SortedMap[Long, Map[String, Map[String, Int]]]
  ): Done = {
    state = fn(state)
    Done
  }

}
