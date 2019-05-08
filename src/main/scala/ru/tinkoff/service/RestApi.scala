package ru.tinkoff.service

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import akka.util.Timeout
import akka.pattern.ask

import scala.concurrent.{ExecutionContext, Future}

case class PageParams(page: Int, pageSize: Int)

case class Order(o: Option[String]) {
  def order = o.filter(s => "asc".equals(s) || "desc".equals(s)).getOrElse("asc")
}

class RestApi(system: ActorSystem, timeout: Timeout) extends RestRoutes {

  implicit val requestTimeout = timeout

  implicit def executionContext = system.dispatcher

  override def createCatalog(): ActorRef = system.actorOf(Catalog.props, Catalog.name)
}

trait RestRoutes extends CatalogApi with CatalogMarshalling {
  import StatusCodes._

  def routes: Route = testRoute ~ authorsRoute ~ authorRoute ~ authorBooksRoute ~ authorsBookNumberRoute ~
    booksRoute ~ bookRoute

  def withPagination = parameters('page.as[Int] ? 0, 'pageSize.as[Int] ? 10).as(PageParams)

  def testRoute =
    pathPrefix("test") {
      pathEndOrSingleSlash {
        // GET /test
        get {
          onSuccess(Future.successful("test answered")) { body =>
            complete(OK, body)
          }
        }
      }
    }

  def authorsRoute =
    pathPrefix("authors") {
      pathEndOrSingleSlash {
        withPagination { pageParams =>
          get {
            onSuccess(getAuthors(pageParams)) { authors =>
              complete(OK, authors)
            }
          }
        }
      }
    }


  def authorRoute =
    pathPrefix("authors" / LongNumber) { authorId =>
      pathEndOrSingleSlash {
        get {
          onSuccess(getAuthor(authorId)) { authorOpt =>
            authorOpt.map(author => complete(OK, author)).getOrElse(complete(NotFound))
          }
        }
      }
    }

  def authorBooksRoute =
    pathPrefix("authors" / LongNumber / "books") { author =>
      pathEndOrSingleSlash {
        withPagination { pageParams =>
          get {
            onSuccess(getBooksByAuthor(pageParams, author)) { booksOpt =>
              booksOpt.map(books => complete(OK, books)).getOrElse(complete(NotFound))
            }
          }
        }
      }
    }


  def authorsBookNumberRoute =
    pathPrefix("authors" / "booksNumber") {
      pathEndOrSingleSlash {
        withPagination { pageParams =>
          get {
            onSuccess(getAuthorsBookNumber(pageParams)) { authors =>
              complete(OK, authors)
            }
          }
        }
      }
    }


  def booksRoute =
    pathPrefix("books") {
      pathEndOrSingleSlash {
        withPagination { pageParams =>
          get {
            onSuccess(getBooks(pageParams, None)) { books =>
              complete(OK, books)
            }
          }
        }
      } ~ path("withSortByViews") {
        withPagination { pageParams =>
          parameter('order ? ).as(Order) { o =>
            get {
              onSuccess(getBooks(pageParams, Some(o))) { books =>
                complete(OK, books)
              }
            }
          }
        }
      }
    }


  def bookRoute =
    pathPrefix("books" / LongNumber) { bookId =>
      pathEndOrSingleSlash {
        get {
          onSuccess(getBook(bookId)) { bookOpt =>
            bookOpt.map(book => complete(OK, book)).getOrElse(complete(NotFound))
          }
        }
      }
    }

}

trait CatalogApi {
  import Catalog._

  def createCatalog(): ActorRef

  implicit def executionContext: ExecutionContext
  implicit def requestTimeout: Timeout

  lazy val catalog = createCatalog()

  def getAuthors(pageParams: PageParams): Future[Authors] = catalog.ask(GetAuthors(pageParams)).mapTo[Authors]

  def getBooks(pageParams: PageParams, order: Option[Order]): Future[Catalog.Books] =
    catalog.ask(GetBooks(pageParams, order)).mapTo[Books]

  def getBooksByAuthor(pageParams: PageParams, authorId: Long): Future[Option[Books]] =
    catalog.ask(GetBooksByAuthor(pageParams, authorId)).mapTo[Option[Books]]

  def getAuthor(authorId: Long): Future[Option[Author]] = catalog.ask(GetAuthor(authorId)).mapTo[Option[Author]]

  def getBook(bookId: Long): Future[Option[Book]] = catalog.ask(GetBook(bookId)).mapTo[Option[Book]]

  def getAuthorsBookNumber(pageParams: PageParams): Future[Authors] =
    catalog.ask(GetAuthorsBookNumber(pageParams)).mapTo[Authors]

}