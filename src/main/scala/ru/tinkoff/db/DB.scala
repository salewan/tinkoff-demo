package ru.tinkoff.db

import ru.tinkoff.model.Tables.{authors, books}
import slick.dbio.Effect
import slick.jdbc.H2Profile.api._

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration

object DB {

  val db = Database.forConfig("h2mem1")

  def setup(): Unit = {
    var setup: DBIOAction[_, NoStream, Effect.Schema with Effect.Write] = (authors.schema ++ books.schema).create

    Data.authors.foreach(author => { setup = setup >> (authors += author) })
    Data.books.foreach(book => { setup = setup >> (books += book) })

    val setupFuture = db.run(setup)

    val resultFuture = setupFuture.flatMap { _ =>
      //println("Catalog:")
      val q1 = for {
        a <- authors
        b <- books if b.authorId === a.id
      } yield (a.name, b.title, b.viewsCount)

      db.run(q1.result).map(_.foreach(t => ()
        //println(" " + t._1 + "\t" + t._2 + "\t" + t._3)
      ))
    }
    Await.result(resultFuture, Duration.Inf)
  }

}
