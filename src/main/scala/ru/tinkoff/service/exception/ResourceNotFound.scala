package ru.tinkoff.service.exception

class ResourceNotFound[T](res: Class[T], id: Long) extends Exception {

  override def getMessage: String = s"Resource ${res.getSimpleName} with id=${id} does not found."
}