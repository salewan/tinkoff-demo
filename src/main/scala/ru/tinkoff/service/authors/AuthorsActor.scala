package ru.tinkoff.service.authors

import akka.actor.{Actor, ActorLogging}
import akka.pattern.pipe
import ru.tinkoff.db.DB.db
import ru.tinkoff.model.Tables._
import ru.tinkoff.service.params.PageParams
import slick.jdbc.H2Profile.api._

import scala.concurrent.ExecutionContext.Implicits.global

object AuthorsActor {
  case class Author(id: Long, name: String, booksNumber: Option[Int])
  case class Authors(authors: Vector[Author])

  case class GetAuthors(pageParams: PageParams)
  case class GetAuthor(id: Long)
  case class GetAuthorsBookNumber(pageParams: PageParams)
}

class AuthorsActor extends Actor with ActorLogging {
  import AuthorsActor._

  override def receive: Receive = {


    case GetAuthors(pageParams) =>
      val q = authors.drop(pageParams.page * pageParams.pageSize).take(pageParams.pageSize).result
      db.run(q).map(f => Authors(f.map(r => Author(r.id, r.name, None)).toVector) ) pipeTo sender()


    case GetAuthor(id) =>
      val q = authors.filter(_.id === id).result.headOption
      db.run(q).map(f => f.map(r => Author(r.id, r.name, None))) pipeTo sender()


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
  }


}
