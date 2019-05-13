package ru.tinkoff.service.authors

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
import ru.tinkoff.service.params.withPagination

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

@Path("/authors")
class AuthorsService(authors: ActorRef)(implicit executionContext: ExecutionContext) extends CatalogMarshalling {
  import AuthorsActor._

  implicit val timeout = Timeout(2.seconds)

  val route = authorsRoute ~ authorRoute ~ authorsBookNumberRoute


  @GET
  @Operation(
    summary = "Return authors",
    description = "Return authors, support paging",
    parameters = Array(
      new Parameter(name = "pageSize", in = ParameterIn.QUERY, description = "page size", required = false),
      new Parameter(name = "page", in = ParameterIn.QUERY, description = "page number, start from 0", required = false)
    ),
    responses = Array(
      new ApiResponse(responseCode = "200", description = "Authors response",
        content = Array(new Content(schema = new Schema(implementation = classOf[Authors])))),
      new ApiResponse(responseCode = "500", description = "Internal server error")
    )
  )
  def authorsRoute =
    pathPrefix("authors") {
      pathEndOrSingleSlash {
        withPagination { pageParams =>
          get {
            onSuccess(authors.ask(GetAuthors(pageParams)).mapTo[Authors]) { authors =>
              complete(OK, authors)
            }
          }
        }
      }
    }


  @GET
  @Path("/{authorId}")
  @Operation(
    summary = "Return author by id",
    description = "Return author by id",
    parameters = Array(new Parameter(name = "id", in = ParameterIn.PATH, description = "author id")),
    responses = Array(
      new ApiResponse(responseCode = "200", description = "Authors response",
        content = Array(new Content(schema = new Schema(implementation = classOf[Author])))),
      new ApiResponse(responseCode = "500", description = "Internal server error")
    )
  )
  def authorRoute =
    pathPrefix("authors" / LongNumber) { authorId =>
      pathEndOrSingleSlash {
        get {
          onSuccess(authors.ask(GetAuthor(authorId)).mapTo[Option[Author]]) { authorOpt =>
            authorOpt.map(author => complete(OK, author)).getOrElse(failWith(notFound[Author](authorId)))
          }
        }
      }
    }


  @GET
  @Path("/booksNumber")
  @Operation(
    summary = "Return authors with theirs book count",
    description = "Return authors with theirs book count, support paging",
    parameters = Array(
      new Parameter(name = "pageSize", in = ParameterIn.QUERY, description = "page size", required = false),
      new Parameter(name = "page", in = ParameterIn.QUERY, description = "page number, start from 0", required = false)
    ),
    responses = Array(
      new ApiResponse(responseCode = "200", description = "Authors response",
        content = Array(new Content(schema = new Schema(implementation = classOf[Authors])))),
      new ApiResponse(responseCode = "500", description = "Internal server error")
    )
  )
  def authorsBookNumberRoute =
    pathPrefix("authors" / "booksNumber") {
      pathEndOrSingleSlash {
        withPagination { pageParams =>
          get {
            onSuccess(authors.ask(GetAuthorsBookNumber(pageParams)).mapTo[Authors]) { authors =>
              complete(OK, authors)
            }
          }
        }
      }
    }


}
