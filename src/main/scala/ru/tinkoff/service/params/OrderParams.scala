package ru.tinkoff.service.params

case class OrderParams(o: Option[String]) {
  def order = o.filter(s => "asc".equals(s) || "desc".equals(s)).getOrElse("asc")
}
