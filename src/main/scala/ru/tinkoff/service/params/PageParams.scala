package ru.tinkoff.service.params

import ru.tinkoff.service.Const.MAX_PAGE_SIZE

case class PageParams(page: Int, pageSize: Int) {
  require(pageSize <= MAX_PAGE_SIZE, s"Exceeding maximum page size, maximum: $MAX_PAGE_SIZE")
}
