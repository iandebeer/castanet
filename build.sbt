val Scala3   = "3.1.0"
val Scala213 = "2.13.8"

val catsVersion          = "2.7.0"
val ceVersion            = "3.3.3"
val fs2Version           = "3.2.4"
val munitVersion         = "0.7.29"
val munitCEVersion       = "1.0.7"
val munitCheckEffVersion = "0.7.1"
val googleProtoVersion   = "3.19.1"
val circeVersion         = "0.14.1"
val monocleVersion       = "3.1.0"
val scodecVersion        = "1.1.30"
val junitVersion         = "0.11"
val refinedVersion       = "0.9.27"
val dhallVersion          = "0.10.0-M2"

Global / onChangedBuildSource := ReloadOnSourceChanges
Global / scalaVersion         := Scala3
ThisBuild / initialCommands := """
  |import cats._, data._, syntax.all._
  |import cats.effect._, concurrent._, cats.effect.implicits._
  |import fs2.3
  |import fs2.concurrent._
  |import scala.concurrent.duration._
  |import ee.mn8.castanet._
""".stripMargin

lazy val root = project
  .in(file("."))
  .aggregate(core)

lazy val core = project
  .in(file("modules/core"))
  .settings(
    // scalaVersion := Scala3,
    name := "core",
    resolvers += "Local Maven Repository" at "file://" + Path.userHome.absolutePath + "/.m2/repository",
    libraryDependencies ++= Seq(
      "com.47deg"           %% "github4s"            % "0.30.0",
      "org.typelevel"       %% "cats-core"           % catsVersion,
      "co.fs2"              %% "fs2-core"            % fs2Version,
      "co.fs2"              %% "fs2-io"              % fs2Version,
      "org.typelevel"       %% "cats-effect"         % ceVersion,
      "dev.optics"          %% "monocle-core"        % monocleVersion,
      "org.scodec"          %% "scodec-bits"         % scodecVersion,
      "org.scala-lang"      %% "scala3-staging"      % Scala3,
      "org.scalameta"       %% "munit"               % munitVersion       % Test,
      "org.scalameta"       %% "munit-scalacheck"    % munitVersion       % Test,
      "org.typelevel"       %% "munit-cats-effect-3" % munitCEVersion    % Test
    ),
    libraryDependencies ++= Seq(
      "io.circe" %% "circe-yaml",
      "io.circe" %% "circe-core",
      "io.circe" %% "circe-generic",
      "io.circe" %% "circe-parser"
    ).map(_ % circeVersion)
  )


