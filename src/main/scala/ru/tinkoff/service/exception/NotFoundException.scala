package ru.tinkoff.service.exception

class NotFoundException(resourceName: String, resourceId: Long) extends Exception {

  override def getMessage: String = s"Resource $resourceName with id=$resourceId is not found."
}
