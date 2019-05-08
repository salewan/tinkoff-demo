package ru.tinkoff.model.entities

import slick.jdbc.H2Profile.api._

case class Author(id: Long, name: String)

class Authors(tag: Tag) extends Table[Author](tag, "authors") {

  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def name = column[String]("name")

  def * = (id, name) <> (Author.tupled, Author.unapply)

}
