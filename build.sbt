val Scala3   = "3.0.2"
val Scala213 = "2.13.6"

val catsVersion          = "2.6.1"
val ceVersion            = "3.2.7"
val fs2Version           = "3.1.1"
val munitVersion         = "0.7.29"
val muniteCEVersion      = "1.0.5"
val munitCheckEffVersion = "0.7.1"
val grpcVersion          = "1.40.1"
val googleProtoVersion   = "3.17.3"
val circeVersion         = "0.14.1"
val monocleVersion       = "3.1.0"
val scodecVersion        = "1.1.28"
val junitVersion         = "0.11"
val refinedVersion       = "0.9.27"
val dhallVersion          = "0.10.0-M2"

Global / onChangedBuildSource := ReloadOnSourceChanges
Global / scalaVersion         := Scala3
ThisBuild / initialCommands := """
  |import cats._, data._, syntax.all._
  |import cats.effect._, concurrent._, cats.effect.implicits._
  |import fs2._
  |import fs2.concurrent._
  |import scala.concurrent.duration._
  |import ee.mn8.castanet._
""".stripMargin

lazy val root = project
  .in(file("."))
  .aggregate(core, protocol, server, client)

lazy val core = project
  .in(file("modules/core"))
  .settings(
    // scalaVersion := Scala3,
    name := "core",
    resolvers += "Local Maven Repository" at "file://" + Path.userHome.absolutePath + "/.m2/repository",
    libraryDependencies ++= Seq(
      "org.typelevel"       %% "cats-core"           % catsVersion,
      "co.fs2"              %% "fs2-core"            % fs2Version,
      "co.fs2"              %% "fs2-io"              % fs2Version,
      "org.typelevel"       %% "cats-effect"         % ceVersion,
      "com.google.protobuf"  % "protobuf-java"       % googleProtoVersion % "protobuf",
      "io.grpc"              % "grpc-netty"          % grpcVersion,
      "io.grpc"              % "grpc-core"           % grpcVersion,
      "io.grpc"              % "grpc-services"       % grpcVersion,
      "dev.optics"          %% "monocle-core"        % monocleVersion,
      "org.scodec"          %% "scodec-bits"         % scodecVersion,
      "org.scala-lang"      %% "scala3-staging"      % Scala3,
      "org.scalameta"       %% "munit"               % munitVersion       % Test,
      "org.scalameta"       %% "munit-scalacheck"    % munitVersion       % Test,
      "org.typelevel"       %% "munit-cats-effect-3" % muniteCEVersion    % Test
    ),
    libraryDependencies ++= Seq(
      "io.circe" %% "circe-yaml",
      "io.circe" %% "circe-core",
      "io.circe" %% "circe-generic",
      "io.circe" %% "circe-parser"
    ).map(_ % circeVersion)
  ).dependsOn(protocol)
  .dependsOn(protocol % "protobuf")

lazy val protocol = project
  .in(file("modules/protocol"))
  .settings(
    name        := "protocol",
    description := "Protobuf definitions",
    resolvers += Resolver.mavenLocal,
    libraryDependencies ++= Seq(
      "com.google.protobuf" % "protobuf-java" % googleProtoVersion % "protobuf"
    )
  )
  .enablePlugins(Fs2Grpc)

lazy val client = project
  .in(file("modules/client"))
  .settings(
    name        := "client",
    description := "Protobuf Client",
    libraryDependencies ++= Seq(
      // "org.dhallj"          %% "dhall-scala"         % dhallVersion,
      "org.dhallj"           % "dhall-imports-mini"  % dhallVersion,
      "org.dhallj"           % "dhall-yaml"          % dhallVersion,
      "org.dhallj"          %% "dhall-circe"         % dhallVersion, 
      "org.scalameta"       %% "munit"               % munitVersion    % Test,
      "org.scalameta"       %% "munit-scalacheck"    % munitVersion    % Test,
      "org.typelevel"       %% "munit-cats-effect-3" % muniteCEVersion % Test,
      "io.grpc"              % "grpc-core"           % grpcVersion,
      "io.grpc"              % "grpc-netty-shaded"   % grpcVersion
    ),
    scalapbCodeGeneratorOptions += CodeGeneratorOption.FlatPackage
  )
  .enablePlugins(Fs2Grpc)
  .dependsOn(protocol,core)
  .dependsOn(protocol % "protobuf")

lazy val server = project
  .in(file("modules/server"))
  .settings(
    scalaVersion := Scala3,
    name         := "server",
    description  := "Protobuf Server",
    libraryDependencies ++= List(
      "io.grpc" % "grpc-netty-shaded" % grpcVersion,
      "io.grpc" % "grpc-core"         % grpcVersion,
      "io.grpc" % "grpc-services"     % grpcVersion
    ),
    scalapbCodeGeneratorOptions += CodeGeneratorOption.FlatPackage
  )
  .enablePlugins(Fs2Grpc)
  .dependsOn(protocol)
  .dependsOn(protocol % "protobuf")
