import akka.actor.{ActorRef, Props}
import akka.http.scaladsl.model.{ContentTypes, StatusCodes}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.util.Timeout
import org.scalatest.{Matchers, WordSpec}
import ru.tinkoff.db.Data
import ru.tinkoff.service.Catalog.Books
import ru.tinkoff.service.Const._
import ru.tinkoff.service.{CatalogMarshalling, Error, RestApi}

import scala.concurrent.duration._
import scala.util.Random

class RestServiceSpec extends WordSpec with Matchers with ScalatestRouteTest with CatalogMarshalling {

  val route = new RestApi(system, Timeout(10.seconds)) {
    override def createCatalog(): ActorRef = system.actorOf(Props[ErsatzCatalog], "ersatz-catalog")
  }.routes


  "Rest api" should {
    "correct handle an unknown path" in {
      Get("/some/unknown/path") ~> route ~> check {
        status shouldEqual StatusCodes.NotFound
        contentType shouldEqual ContentTypes.`application/json`
        responseAs[Error].message shouldEqual
          s"The path you requested [/some/unknown/path] does not exist."

      }
    }
  }

  "Requests by non existing identifiers" should {
    "be handled correctly" in {
      Get("/authors/100500") ~> route ~> check {
        status shouldEqual StatusCodes.BadRequest
        contentType shouldEqual ContentTypes.`application/json`
        responseAs[Error].message shouldEqual
          s"Resource Author with id=100500 is not found."
      }
    }
  }

  "Requesting pageable data with specified page parameters" should {
    "be answered with appropriate error when exceeding the pageSize" in {
      Get(s"/authors?pageSize=${MAX_PAGE_SIZE + 1}") ~> route ~> check {
        status shouldEqual StatusCodes.BadRequest
        contentType shouldEqual ContentTypes.`application/json`
        responseAs[Error].message shouldEqual
          s"requirement failed: Exceeding maximum page size, maximum: $MAX_PAGE_SIZE"
      }
    }

    "be answered with appropriate error when got zero pageSize" in {
      Get(s"/authors/booksNumber?pageSize=0") ~> route ~> check {
        status shouldEqual StatusCodes.BadRequest
        contentType shouldEqual ContentTypes.`application/json`
        responseAs[Error].message shouldEqual
          s"requirement failed: Minimum page size: $MIN_PAGE_SIZE"
      }
    }

    val ps = Random.nextInt(MAX_PAGE_SIZE) + 1
    s"be answered with a correct length array ($ps)" in {
      Get(s"/books?pageSize=$ps") ~> route ~> check {
        status shouldEqual StatusCodes.OK
        contentType shouldEqual ContentTypes.`application/json`
        responseAs[Books].books.size shouldEqual ps
      }
    }
  }

  "Ordering" should {
    "give sorted by ascending collection by default" in {
      Get(s"/books/withSortByViews") ~> route ~> check {
        status shouldEqual StatusCodes.OK
        contentType shouldEqual ContentTypes.`application/json`
        val expecting = Data.books.sortBy(_.viewsCount).map(_.id).take(DEFAULT_PAGE_SIZE).toVector
        responseAs[Books].books.map(_.id) shouldEqual expecting
      }
    }

    "give sorted by descending collection when the appropriate parameter is specified" in {
      Get(s"/books/withSortByViews?order=desc") ~> route ~> check {
        status shouldEqual StatusCodes.OK
        contentType shouldEqual ContentTypes.`application/json`
        val expecting = Data.books.sortBy(- _.viewsCount).map(_.id).take(DEFAULT_PAGE_SIZE).toVector
        responseAs[Books].books.map(_.id) shouldEqual expecting
      }
    }
  }

  "Requesting pageable data" should {
    "not fail when the request is out of bounds of underlying collection" in {
      Get(s"/books?pageSize=10&page=10000") ~> route ~> check {
        status shouldEqual StatusCodes.OK
        contentType shouldEqual ContentTypes.`application/json`
        responseAs[Books].books.size shouldEqual 0
      }
    }
  }

}