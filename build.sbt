ThisBuild / scalaVersion := "2.13.12"
ThisBuild / organization := "io.github.valdemargr"

ThisBuild / licenses := List(
  "Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")
)
ThisBuild / developers := List(
  Developer(
    "valdemargr",
    "Valdemar Grange",
    "randomvald0069@gmail.com",
    url("https://github.com/valdemargr")
  )
)
lazy val sharedSettings = Seq(
  organization := "io.github.valdemargr",
  organizationName := "Valdemar Grange",
  scalacOptions ++= {
    if (scalaVersion.value.startsWith("2")) {
      Seq(
        "-Wunused:-nowarn",
        "-Wconf:cat=unused-nowarn:s",
        "-Ywarn-unused:-nowarn"
      )
    } else Seq.empty // Seq("-explain")
  },
  libraryDependencies ++= Seq(
    "org.typelevel" %% "cats-effect" % "3.5.2",
    "org.typelevel" %% "cats-core" % "2.9.0",
    "org.typelevel" %% "cats-parse" % "0.3.8",
    "org.typelevel" %% "vault" % "3.5.0",
    "io.circe" %% "circe-core" % "0.14.6",
    "io.circe" %% "circe-parser" % "0.14.6",
    "io.circe" %% "circe-generic" % "0.14.6",
    "org.http4s" %% "http4s-circe" % "1.0.0-M40",
    "org.http4s" %% "http4s-dsl" % "1.0.0-M40",
    "org.http4s" %% "http4s-client" % "1.0.0-M40",
    "org.http4s" %% "http4s-server" % "1.0.0-M40",
    "org.http4s" %% "http4s-ember-server" % "1.0.0-M40",
    "org.http4s" %% "http4s-ember-client" % "1.0.0-M40",
    "ch.qos.logback" % "logback-classic" % "1.2.9",
    "co.fs2" %% "fs2-core" % "3.9.3",
    "co.fs2" %% "fs2-io" % "3.9.3",
    "org.tpolecat" %% "sourcepos" % "1.1.0",
    "io.github.valdemargr" %% "catch-effect" % "0.0.1",
    "org.typelevel" %% "kittens" % "3.1.0",
    "org.scalameta" %% "munit" % "1.0.0-M10" % Test,
    "org.typelevel" %% "munit-cats-effect" % "2.0.0-M3" % Test,
    "com.monovore" %% "decline" % "2.4.1",
    "com.monovore" %% "decline-effect" % "2.4.1"
  )
)

lazy val client = project
  .in(file("client"))
  .settings(sharedSettings)
  .settings(
    name := "client",
    fork := true
  )
lazy val server = project
  .in(file("server"))
  .settings(sharedSettings)
  .settings(
    name := "server",
    fork := true
  )