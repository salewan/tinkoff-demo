package ru.tinkoff.service

import ru.tinkoff.service.authors.AuthorsActor._
import ru.tinkoff.service.books.BooksActor._

package object exception {

  def authorNotFound(id: Long) = new ResourceNotFound[Author](classOf[Author], id)

  def bookNotFound(id: Long) = new ResourceNotFound[Book](classOf[Book], id)
}
