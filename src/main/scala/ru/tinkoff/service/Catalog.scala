package ru.tinkoff.service

import akka.actor.{Actor, Props}
import akka.event.Logging
import akka.pattern.pipe
import akka.util.Timeout
import ru.tinkoff.db.DB
import ru.tinkoff.model.Tables._
import ru.tinkoff.service.Catalog._
import slick.jdbc.H2Profile.api._
import ru.tinkoff.service.params._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

object Catalog {

  def props(implicit timeout: Timeout) = Props(new Catalog)
  def name = "catalog"

  case class Author(id: Long, name: String, booksNumber: Option[Int])
  case class Authors(authors: Vector[Author])
  case class Book(id: Long, authorId: Long, title: String, viewsCount: Long)
  case class Books(books: Vector[Book])

  case class GetAuthors(pageParams: PageParams)
  case class GetBooks(pageParams: PageParams, order: Option[OrderParams] = None)
  case class GetBooksByAuthor(pageParams: PageParams, authorId: Long)
  case class GetAuthor(id: Long)
  case class GetBook(id: Long)
  case class GetAuthorsBookNumber(pageParams: PageParams)
}

class Catalog(implicit timeout: Timeout) extends Actor {
  import DB.db

  val log =  Logging(context.system.eventStream, "catalog")

  override def receive: Receive = {

    case GetAuthors(pageParams) =>
      val q = authors.drop(pageParams.page * pageParams.pageSize).take(pageParams.pageSize).result
      db.run(q).map(f => Authors(f.map(r => Author(r.id, r.name, None)).toVector) ) pipeTo sender()


    case GetAuthor(id) =>
      val q = authors.filter(_.id === id).result.headOption
      db.run(q).map(f => f.map(r => Author(r.id, r.name, None))) pipeTo sender()


    case GetBooksByAuthor(pageParams, authorId) =>
      val dbFuture =
      db.run(authors.filter(_.id === authorId).result.headOption).flatMap {
        case Some(_) =>
          db.run(
            books.
              filter(_.authorId === authorId).
              drop(pageParams.page * pageParams.pageSize).
              take(pageParams.pageSize).
              result
          ).map(f => Some(Books(f.map(r => Book(r.id, r.authorId, r.title, r.viewsCount)).toVector)))
        case None => Future.successful(None)
      }

      dbFuture pipeTo sender()


    case GetBook(id) =>
      db.run(books.filter(_.id === id).result.headOption).map {
        case Some(b) =>
          db.run(
            books.
              filter(f => f.id === id && f.viewsCount === b.viewsCount).map(_.viewsCount).update(b.viewsCount + 1).
              asTry.
              flatMap {
                case Failure(e: Throwable) =>
                  log.error(s"Cannot increment Book#viewsCount, id#$id")
                  DBIO.failed(e)
                case Success(_) => DBIO.successful(1)
              }.transactionally
          )
          Some(Book(b.id, b.authorId, b.title, b.viewsCount))
        case None => None
      } pipeTo sender()


    case GetAuthorsBookNumber(pageParams) =>
      val q = (for {
        b <- books
        a <- b.fkAuthor
      } yield (a, b)).groupBy(_._2.authorId)

      val q2 = q.map { case (authorId, bas) =>
        (authorId, bas.length)
      }

      val q3 = for {
        rs <- q2
        a <- authors.filter(_.id === rs._1)
      } yield (a, rs._2)

      val fut = db.run(q3.drop(pageParams.page * pageParams.pageSize).take(pageParams.pageSize).result).map {seq =>
        Authors(seq.map { case (a, cnt) => Author(a.id, a.name, Some(cnt)) }.toVector)
      }

      fut pipeTo sender()


    case GetBooks(pageParams, order) =>
      val q = order.map { o =>
        if (o.order.equals("asc")) books.sortBy(_.viewsCount.asc)
        else books.sortBy(_.viewsCount.desc)
      }.getOrElse(books)

      val fut = db.run(q.drop(pageParams.page * pageParams.pageSize).take(pageParams.pageSize).result).map { seq =>
        Books(seq.map(b => Book(b.id, b.authorId, b.title, b.viewsCount)).toVector)
      }

      fut pipeTo sender()
  }
}
