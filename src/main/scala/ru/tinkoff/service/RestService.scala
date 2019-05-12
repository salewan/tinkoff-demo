package ru.tinkoff.service

import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives.{handleExceptions, handleRejections}
import akka.http.scaladsl.server.{Route, RouteConcatenation}
import akka.stream.ActorMaterializer
import ch.megard.akka.http.cors.scaladsl.CorsDirectives.cors
import ru.tinkoff.db.DB
import ru.tinkoff.service.authors.{AuthorsActor, AuthorsService}
import ru.tinkoff.service.books.{BooksActor, BooksService}
import ru.tinkoff.swagger.SwaggerDocService

import scala.io.StdIn
import scala.util.Try

object RestService extends App with RouteConcatenation {

  DB.setup()

  implicit val system = ActorSystem("my-system")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  val books = system.actorOf(Props[BooksActor])
  val authors = system.actorOf(Props[AuthorsActor])

  val route: Route = wrapRoutes(
    new BooksService(books).route ~
      new AuthorsService(authors).route ~
      SwaggerDocService.routes
  )

  val port = Try(system.settings.config.getInt("akka.http.server.port")).getOrElse(8080)
  print(port)
  val bindingFuture = Http().bindAndHandle(route, "0.0.0.0", port)

  println("Application has started on port 8080")

  StdIn.readLine()
  bindingFuture
    .flatMap(_.unbind())
    .onComplete(_ => system.terminate())

  def wrapRoutes(route: Route)(implicit system: ActorSystem): Route = {
    handleRejections(myRejectionHandler) {
      handleExceptions(myExceptionHandler) {
        cors()(
          route
        )
      }
    }
  }

}
