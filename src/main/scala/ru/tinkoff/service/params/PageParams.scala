package ru.tinkoff.service.params

import ru.tinkoff.service.Const._

case class PageParams(page: Int, pageSize: Int) {
  require(pageSize <= MAX_PAGE_SIZE, s"Exceeding maximum page size, maximum: $MAX_PAGE_SIZE")
  require(pageSize > 0, s"Minimum page size: $MIN_PAGE_SIZE")
}
