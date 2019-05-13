package ru.tinkoff.service

import scala.reflect.ClassTag

package object exception {

  def notFound[A : ClassTag](id: Long): NotFoundException = {
    val ct = implicitly[ClassTag[A]]
    new NotFoundException(ct.runtimeClass.getSimpleName, id)
  }
}
