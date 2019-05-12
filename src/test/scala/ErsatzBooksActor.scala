import akka.actor.Actor
import ru.tinkoff.db.Data
class ErsatzBooksActor extends Actor with Common {
  import ru.tinkoff.service.books.BooksActor._

  var books = Data.books
  var authors = Data.authors

  override def receive: Receive = {

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


    case GetBooks(pageParams, o) =>
      val seq = o.map { ord =>
        val order = ord.order
        if (order.equals("asc")) sliceCollection(pageParams, books.sortBy(_.viewsCount))
        else sliceCollection(pageParams, books.sortBy(- _.viewsCount))
      }.getOrElse(sliceCollection(pageParams, books))

      sender() ! Books(seq.map(b => Book(b.id, b.authorId, b.title, b.viewsCount)).toVector)
  }

}
