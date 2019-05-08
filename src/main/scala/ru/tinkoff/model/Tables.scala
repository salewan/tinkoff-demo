package ru.tinkoff.model

import ru.tinkoff.model.entities.{Authors, Books}
import slick.jdbc.H2Profile.api._

object Tables {
  val books = TableQuery[Books]
  val authors = TableQuery[Authors]
}
