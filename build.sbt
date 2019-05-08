name := "testRestApiService"

version := "1.0"

scalaVersion := "2.12.2"

libraryDependencies ++= {
  val akkaHttpV = "10.0.5"
  val slickV = "3.2.0"
  val scalaTestV = "3.0.1"
  val h2V = "1.4.193"
  Seq(
    "com.typesafe.akka" %% "akka-http" % akkaHttpV,
    "com.typesafe.slick" %% "slick" % slickV,
    "com.h2database" % "h2" % h2V,
    "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpV % "test",
    "org.scalatest" %% "scalatest" % scalaTestV % "test"
  )
}

// https://mvnrepository.com/artifact/it.bitbl/scala-faker
libraryDependencies += "it.bitbl" %% "scala-faker" % "0.4"

libraryDependencies += "com.typesafe.akka" %% "akka-http-spray-json" % "10.0.5"