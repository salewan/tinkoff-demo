package ru.tinkoff.service.books

import akka.actor.{Actor, ActorLogging}
import akka.pattern.pipe
import ru.tinkoff.db.DB.db
import ru.tinkoff.model.Tables._
import ru.tinkoff.service.params.{OrderParams, PageParams}
import slick.jdbc.H2Profile.api._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

object BooksActor {
  case class Book(id: Long, authorId: Long, title: String, viewsCount: Long)
  case class Books(books: Vector[Book])

  case class GetBooks(pageParams: PageParams, order: Option[OrderParams] = None)
  case class GetBooksByAuthor(pageParams: PageParams, authorId: Long)
  case class GetBook(id: Long)
}

class BooksActor extends Actor with ActorLogging {
  import BooksActor._

  override def receive: Receive = {


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
