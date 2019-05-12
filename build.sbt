enablePlugins(JavaAppPackaging)

name := "testRestApiService"

version := "1.0"

scalaVersion := "2.12.2"

libraryDependencies ++= {
  val akkaHttpV = "10.0.5"
  val slickV = "3.2.0"
  val scalaTestV = "3.0.1"
  val h2V = "1.4.193"
  val swaggerV = "2.0.8"
  
  Seq(
    "com.typesafe.akka" %% "akka-http" % akkaHttpV,
    "com.typesafe.slick" %% "slick" % slickV,
    "com.h2database" % "h2" % h2V,
    "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpV % "test",
    "org.scalatest" %% "scalatest" % scalaTestV % "test",
    
    "com.typesafe.akka" %% "akka-http-spray-json" % "10.0.5",
    "it.bitbl" %% "scala-faker" % "0.4",
    "javax.ws.rs" % "javax.ws.rs-api" % "2.0.1",
    "com.github.swagger-akka-http" %% "swagger-akka-http" % "2.0.2",
    "com.github.swagger-akka-http" %% "swagger-scala-module" % "2.0.3",
    "io.swagger.core.v3" % "swagger-core" % swaggerV,
    "io.swagger.core.v3" % "swagger-annotations" % swaggerV,
    "io.swagger.core.v3" % "swagger-models" % swaggerV,
    "io.swagger.core.v3" % "swagger-jaxrs2" % swaggerV,
    "ch.megard" %% "akka-http-cors" % "0.4.0",
  )
}

mainClass in assembly := Some("ru.tinkoff.service.RestService")
assemblyJarName in assembly := "testRestApiService.jar"