package ru.tinkoff.swagger

import com.github.swagger.akka.SwaggerHttpService
import com.github.swagger.akka.model.Info
import io.swagger.v3.oas.models.ExternalDocumentation
import ru.tinkoff.service.authors.AuthorsService
import ru.tinkoff.service.books.BooksService

class SwaggerDocService(h: String, p: Int) extends SwaggerHttpService {
  override val apiClasses = Set(classOf[AuthorsService], classOf[BooksService])

  override def schemes: List[String] = List("https")
  override val host = s"pure-shore-41705.herokuapp.com"
  override val info = Info(version = "1.0")
  override val externalDocs = Some(new ExternalDocumentation().description("Core Docs").url("http://acme.com/docs"))
  //override val securitySchemeDefinitions = Map("basicAuth" -> new BasicAuthDefinition())
  override val unwantedDefinitions = Seq("Function1", "Function1RequestContextFutureRouteResult")
}