package ru.tinkoff.swagger

import com.github.swagger.akka.SwaggerHttpService
import com.github.swagger.akka.model.Info
import io.swagger.v3.oas.models.ExternalDocumentation
import ru.tinkoff.service.authors.AuthorsService
import ru.tinkoff.service.books.BooksService

object SwaggerDocService extends SwaggerHttpService {
  override val apiClasses = Set(classOf[AuthorsService], classOf[BooksService])
  override val host = "localhost:8080"
  override val info = Info(version = "1.0")
  override val externalDocs = Some(new ExternalDocumentation().description("Core Docs").url("http://acme.com/docs"))
  //override val securitySchemeDefinitions = Map("basicAuth" -> new BasicAuthDefinition())
  override val unwantedDefinitions = Seq("Function1", "Function1RequestContextFutureRouteResult")
}