package ru.tinkoff.service

import akka.http.scaladsl.server.Directives.{parameters, _}
import ru.tinkoff.service.Const._

package object params {

  def withPagination =
    parameters('page.as[Int] ? DEFAULT_PAGE, 'pageSize.as[Int] ? DEFAULT_PAGE_SIZE).as(PageParams)
}
