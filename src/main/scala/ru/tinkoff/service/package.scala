package ru.tinkoff

import akka.http.scaladsl.model.StatusCodes.{BadRequest, InternalServerError, NotFound}
import akka.http.scaladsl.server.Directives.{complete, extractUnmatchedPath}
import akka.http.scaladsl.server.{ExceptionHandler, RejectionHandler, ValidationRejection}
import ru.tinkoff.service.exception.NotFoundException

package object service extends CatalogMarshalling {

  val myExceptionHandler = ExceptionHandler {
    case e: NotFoundException =>
      complete(BadRequest, Error(e.getMessage))
    case e: Throwable =>
      complete(InternalServerError, Error(e.getMessage))
  }

  val myRejectionHandler: RejectionHandler = RejectionHandler.newBuilder()
    .handle { case vr: ValidationRejection =>
      complete(BadRequest, Error(vr.message))
    }
    .handleNotFound {
      extractUnmatchedPath { p =>
        complete(NotFound, Error(s"The path you requested [$p] does not exist."))
      }
    }
    .result()
}
