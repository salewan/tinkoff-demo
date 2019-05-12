import akka.actor.Actor
import akka.pattern.pipe
import ru.tinkoff.db.Data

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ErsatzAuthorsActor extends Actor with Common {
  import ru.tinkoff.service.authors.AuthorsActor._

  var books = Data.books
  var authors = Data.authors

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


    case GetAuthorsBookNumber(pageParams) =>
      sender() ! sliceCollection(pageParams,
        authors
          .map(a => Author(a.id, a.name, Some(books.count(_.authorId == a.id))))
          .toVector
      )
  }
}
