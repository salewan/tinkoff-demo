package ru.tinkoff.db

import ru.tinkoff.model.Tables.{authors, books}
import ru.tinkoff.model.entities.{Author, Book}
import slick.dbio.Effect
import slick.jdbc.H2Profile.api._

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.util.Random

object DB {

  val db = Database.forConfig("h2mem1")
  val AUTHORS = 100
  val MAX_BOOKS_PER_AUTHOR = 16

  def setup(): Unit = {
    var setup: DBIOAction[_, NoStream, Effect.Schema with Effect.Write] = (authors.schema ++ books.schema).create

    (1 to AUTHORS).map(author_id => Author(author_id, faker.Name.name)).foreach {author =>
      setup = setup >> (authors += author)
      (1 to Random.nextInt(MAX_BOOKS_PER_AUTHOR))
        .map(bookId => Book(bookId, author.id, faker.Lorem.words().mkString(" ").capitalize, 0))
        .foreach(book => setup = setup >> (books += book))
    }

    val setupFuture = db.run(setup)

    val resultFuture = setupFuture.flatMap { _ =>
      println("Catalog:")
      val q1 = for {
        a <- authors
        b <- books if b.authorId === a.id
      } yield (a.name, b.title, b.viewsCount)

      db.run(q1.result).map(_.foreach(t =>
        println(" " + t._1 + "\t" + t._2 + "\t" + t._3)
      ))
    }
    Await.result(resultFuture, Duration.Inf)
  }

}
