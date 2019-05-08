package ru.tinkoff.service

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import akka.util.Timeout
import ru.tinkoff.db.DB

import scala.concurrent.duration._
import scala.util.Try

object RestService extends App {

  DB.setup()

  implicit val system = ActorSystem("my-system")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  val route: Route = new RestApi(system, Timeout(10.seconds)).routes

  val port = Try(system.settings.config.getInt("akka.http.server.port")).getOrElse(8080)
  print(port)
  val bindingFuture = Http().bindAndHandle(route, "localhost", port)

  bindingFuture.onComplete {
    case scala.util.Failure(exception) =>
      system.terminate()
    case scala.util.Success(value) =>
      println("Server successfully started at 8080")
  }

}
