package ru.tinkoff.service.books

import akka.actor.ActorRef
import akka.http.scaladsl.model.StatusCodes.OK
import akka.http.scaladsl.server.Directives._
import akka.pattern.ask
import akka.util.Timeout
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.{Content, Schema}
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.{Operation, Parameter}
import javax.ws.rs.{GET, Path}
import ru.tinkoff.service.CatalogMarshalling
import ru.tinkoff.service.exception._
import ru.tinkoff.service.params.{OrderParams, withPagination}
import ru.tinkoff.service.authors.AuthorsActor.Author

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

@Path("/books")
class BooksService(books: ActorRef)(implicit executionContext: ExecutionContext) extends CatalogMarshalling {
  import BooksActor._

  implicit val timeout = Timeout(2.seconds)

  val route = booksByAuthorRoute ~ booksRoute ~ booksSortedRoute ~ bookRoute


  @GET
  @Path("/author/{authorId}")
  @Operation(
    summary = "Return books by author",
    description = "Return books by author, support paging",
    parameters =  Array(
      new Parameter(name = "pageSize", in = ParameterIn.QUERY, description = "page size", required = false),
      new Parameter(name = "page", in = ParameterIn.QUERY, description = "page number, start from 0", required = false),
      new Parameter(name = "id", in = ParameterIn.PATH, description = "author id", required = true),
    ),
    responses = Array(
      new ApiResponse(responseCode = "200", description = "Authors response",
        content = Array(new Content(schema = new Schema(implementation = classOf[Books])))),
      new ApiResponse(responseCode = "500", description = "Internal server error")
    )
  )
  def booksByAuthorRoute =
    pathPrefix("books" / "author" / LongNumber) { authorId =>
      pathEndOrSingleSlash {
        withPagination { pageParams =>
          get {
            onSuccess(books.ask(GetBooksByAuthor(pageParams, authorId)).mapTo[Option[Books]]) { booksOpt =>
              booksOpt.map(books => complete(OK, books)).getOrElse(failWith(notFound[Author](authorId)))
            }
          }
        }
      }
    }


  @GET
  @Operation(
    summary = "Return books",
    description = "Return books, support paging",
    parameters = Array(
      new Parameter(name = "pageSize", in = ParameterIn.QUERY, description = "page size", required = false),
      new Parameter(name = "page", in = ParameterIn.QUERY, description = "page number, start from 0", required = false),
    ),
    responses = Array(
      new ApiResponse(responseCode = "200", description = "Authors response",
        content = Array(new Content(schema = new Schema(implementation = classOf[Books])))),
      new ApiResponse(responseCode = "500", description = "Internal server error")
    )
  )
  def booksRoute =
    pathPrefix("books") {
      pathEndOrSingleSlash {
        withPagination { pageParams =>
          get {
            onSuccess(books.ask(GetBooks(pageParams, None)).mapTo[Books]) { books =>
              complete(OK, books)
            }
          }
        }
      }
    }


  @GET
  @Path("/withSortByViews")
  @Operation(
    summary = "Return books, sorted by theirs views count",
    description = "Return books, sorted by theirs views count, support paging",
    parameters = Array(
      new Parameter(name = "pageSize", in = ParameterIn.QUERY, description = "page size", required = false),
      new Parameter(name = "page", in = ParameterIn.QUERY, description = "page number, start from 0", required = false),
      new Parameter(name = "order", in = ParameterIn.QUERY, description = "sort order {asc/desc}", required = false)
    ),
    responses = Array(
      new ApiResponse(responseCode = "200", description = "Authors response",
        content = Array(new Content(schema = new Schema(implementation = classOf[Books])))),
      new ApiResponse(responseCode = "500", description = "Internal server error")
    )
  )
  def booksSortedRoute =
    pathPrefix("books") {
      path("withSortByViews") {
        withPagination { pageParams =>
          parameter('order ? ).as(OrderParams) { o =>
            get {
              onSuccess(books.ask(GetBooks(pageParams, Some(o))).mapTo[Books]) { books =>
                complete(OK, books)
              }
            }
          }
        }
      }
    }


  @GET
  @Path("/{bookId}")
  @Operation(
    summary = "Return book",
    description = "Return book by id",
    parameters = Array(
      new Parameter(name = "id", in = ParameterIn.PATH, description = "book id", required = true)
    ),
    responses = Array(
      new ApiResponse(responseCode = "200", description = "Authors response",
        content = Array(new Content(schema = new Schema(implementation = classOf[Book])))),
      new ApiResponse(responseCode = "500", description = "Internal server error")
    )
  )
  def bookRoute =
    pathPrefix("books" / LongNumber) { bookId =>
      pathEndOrSingleSlash {
        get {
          onSuccess(books.ask(GetBook(bookId)).mapTo[Option[Book]]) { bookOpt =>
            bookOpt.map(book => complete(OK, book)).getOrElse(failWith(notFound[Book](bookId)))
          }
        }
      }
    }


}
