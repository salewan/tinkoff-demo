package ru.tinkoff.service

import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives.{handleExceptions, handleRejections}
import akka.http.scaladsl.server.{Route, RouteConcatenation}
import akka.stream.ActorMaterializer
import ch.megard.akka.http.cors.scaladsl.CorsDirectives.cors
import com.typesafe.config.ConfigFactory
import ru.tinkoff.db.DB
import ru.tinkoff.service.authors.{AuthorsActor, AuthorsService}
import ru.tinkoff.service.books.{BooksActor, BooksService}
import ru.tinkoff.swagger.SwaggerDocService

object RestService extends App with RouteConcatenation {

  DB.setup()

  implicit val system = ActorSystem("my-system")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  val config = ConfigFactory.load()
  val host = config.getString("http.host")
  val port = config.getInt("http.port")

  val books = system.actorOf(Props[BooksActor])
  val authors = system.actorOf(Props[AuthorsActor])

  val route: Route = wrapRoutes(
    new BooksService(books).route ~
      new AuthorsService(authors).route ~
      new SwaggerDocService(host, port).routes
  )

  val bindingFuture = Http().bindAndHandle(route, host, port)

  bindingFuture.onComplete {
    case scala.util.Failure(exception) =>
      println(exception)
      system.terminate()
    case scala.util.Success(value) =>
      println(s"Application has started on port $host:$port")
  }

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
