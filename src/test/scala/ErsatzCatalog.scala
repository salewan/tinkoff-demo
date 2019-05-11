import akka.actor.Actor
import akka.pattern.pipe
import ru.tinkoff.model.entities

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class ErsatzCatalog extends Actor {
  import ru.tinkoff.service.Catalog._
  import ErsatzCatalog.{authors, books}

  override def receive: Receive = {
    case GetAuthors(pageParams) =>
      Future.successful {
        Authors(
          authors
            .slice(pageParams.page * pageParams.pageSize, pageParams.page * pageParams.pageSize + pageParams.pageSize)
            .map(a => Author(a.id, a.name, None))
            .toVector
        )
      }.pipeTo(sender())
  }
}

object ErsatzCatalog {
  import ru.tinkoff.db.Data

  var books = Data.books
  var authors = Data.authors
}