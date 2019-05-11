package ru.tinkoff.service

import ru.tinkoff.service.Catalog.{Author, Book}

package object exception {

  def authorNotFound(id: Long) = new ResourceNotFound[Author](classOf[Author], id)

  def bookNotFound(id: Long) = new ResourceNotFound[Book](classOf[Book], id)
}
