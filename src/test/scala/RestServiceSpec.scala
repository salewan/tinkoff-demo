import akka.actor.{Actor, ActorRef, Props}
import akka.http.scaladsl.model.{ContentTypes, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.util.Timeout
import org.scalatest.{Matchers, WordSpec}
import ru.tinkoff.service.Catalog.Authors
import ru.tinkoff.service.RestApi

import scala.concurrent.Future
import scala.concurrent.duration._


class RestServiceSpec extends WordSpec with Matchers with ScalatestRouteTest {

  val smallRoute =
    get {
      pathSingleSlash {
        complete {
          "Captain on the bridge!"
        }
      } ~
        path("ping") {
          complete("PONG!")
        }
    }

  "The service" should {
    "return a greeting for GET requests to the root path" in {
      // tests:
      Get() ~> smallRoute ~> check {
        responseAs[String] shouldEqual "Captain on the bridge!"
      }
    }
  }


  val route = new RestApi(system, Timeout(10.seconds)) {
    override def createCatalog(): ActorRef = system.actorOf(Props[ErsatzCatalog], "ersatz-catalog")
  }.routes

  "Test server" should {
    "return something" in {
      Get("/authors") ~> route ~> check {
        status shouldEqual StatusCodes.OK
        contentType shouldEqual ContentTypes.`application/json`
      }
    }
  }

}