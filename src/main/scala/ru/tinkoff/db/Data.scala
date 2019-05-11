package ru.tinkoff.db

import ru.tinkoff.model.entities.{Author, Book}

import scala.util.Random

object Data {

  val AUTHORS = 100
  val MAX_BOOKS_PER_AUTHOR = 16

  val authors = (1 to AUTHORS).map(author_id => Author(author_id, faker.Name.name))

  val books = authors.flatMap { author =>
    (1 to Random.nextInt(MAX_BOOKS_PER_AUTHOR))
      .map(bookId => Book(bookId, author.id, faker.Lorem.words().mkString(" ").capitalize, 0))
  }
}
