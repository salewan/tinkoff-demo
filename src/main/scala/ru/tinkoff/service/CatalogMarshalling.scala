package ru.tinkoff.service

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json._

case class Error(message: String)

trait CatalogMarshalling extends SprayJsonSupport with DefaultJsonProtocol {
  import Catalog._

  implicit val authorFormat = jsonFormat3(Author)
  implicit val authorsFormat = jsonFormat1(Authors)
  implicit val bookFormat = jsonFormat4(Book)
  implicit val booksFormat = jsonFormat1(Books)
  implicit val errorFormat = jsonFormat1(Error)
}
