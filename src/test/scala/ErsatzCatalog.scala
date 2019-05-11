import akka.actor.Actor
import akka.pattern.pipe
import ru.tinkoff.service.params.PageParams

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ErsatzCatalog extends Actor {
  import ErsatzCatalog.{authors, books}
  import ru.tinkoff.service.Catalog._

  def sliceCollection[T](pp: PageParams, col: Seq[T]): Seq[T] = {
    val skip = pp.page * pp.pageSize
    if (col.size > skip) col.drop(skip).take(math.min(pp.pageSize, col.size - skip))
    else Seq.empty
  }

  override def receive: Receive = {
    case GetAuthors(pageParams) =>
      Future.successful {
        Authors(sliceCollection(pageParams,
          authors
            .slice(pageParams.page * pageParams.pageSize, pageParams.page * pageParams.pageSize + pageParams.pageSize)
            .map(a => Author(a.id, a.name, None))
        ).toVector)
      }.pipeTo(sender())

    case GetAuthor(id) =>
      sender() ! authors.find(_.id == id)

    case GetBooksByAuthor(pageParams, authorId) =>
      sender() ! authors.find(_.id == authorId).map { _ =>
        Books(sliceCollection(pageParams,
          books
            .filter(_.authorId == authorId)
            .map(b => Book(b.id, b.authorId, b.title, b.viewsCount))
        ).toVector)

      }.getOrElse(None)

    case GetBook(id) =>
      sender() ! books.find(_.id == id)

    case GetAuthorsBookNumber(pageParams) =>
      sender() ! sliceCollection(pageParams,
        authors
        .map(a => Author(a.id, a.name, Some(books.count(_.authorId == a.id))))
        .toVector
      )

    case GetBooks(pageParams, o) =>
      val seq = o.map { ord =>
        val order = ord.order
        if (order.equals("asc")) sliceCollection(pageParams, books.sortBy(_.viewsCount))
        else sliceCollection(pageParams, books.sortBy(- _.viewsCount))
      }.getOrElse(sliceCollection(pageParams, books))

      sender() ! Books(seq.map(b => Book(b.id, b.authorId, b.title, b.viewsCount)).toVector)

  }
}

object ErsatzCatalog {
  import ru.tinkoff.db.Data

  var books = Data.books
  var authors = Data.authors
}