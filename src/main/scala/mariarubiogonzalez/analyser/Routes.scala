package mariarubiogonzalez.analyser

import akka.actor.ActorSystem
import akka.http.scaladsl.model.StatusCodes.OK
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route

class Routes()(implicit val system: ActorSystem) {

  val metrics: Route =
    get {
      path("metrics") {
        complete(OK)
      }
    }
}
