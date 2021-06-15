val Scala3 = "3.0.0"
val Scala213 = "2.13.5"

val catsVersion = "2.6.1"
val ceVersion = "3.1.1" 
val fs2Version = "3.0.4"
val munitVersion = "0.7.26"
val muniteCEVersion = "1.0.3"
val munitCheckEffVersion = "0.7.1"
val grpcVersion = "1.38.0"
val googleProtoVersion = "3.17.1"
val circeVersion = "0.14.1"
val monocleVersion = "3.0.0-RC2"

Global / onChangedBuildSource := ReloadOnSourceChanges
Global / scalaVersion := Scala3
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
  .aggregate(core, protocol, stt_compiler, server, client) //, docs)

lazy val core = project
  .in(file("modules/core"))
  .settings(
    // scalaVersion := Scala3,
    name := "core",
    resolvers += "Local Maven Repository" at "file://" + Path.userHome.absolutePath + "/.m2/repository",
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-core" % catsVersion,
      "co.fs2" %% "fs2-core" % fs2Version,
      "co.fs2" %% "fs2-io" % fs2Version,
      "org.typelevel" %% "cats-effect" % ceVersion,
      "com.google.protobuf" % "protobuf-java" % googleProtoVersion % "protobuf",
       "io.grpc" % "grpc-netty" % grpcVersion,
      "io.grpc" % "grpc-services" % grpcVersion,
      "dev.optics" %% "monocle-core"  % monocleVersion,
      "org.scalameta" %% "munit" % munitVersion % Test,
      "org.scalameta" %% "munit-scalacheck" % munitVersion % Test,
      "org.typelevel" %% "munit-cats-effect-3" % muniteCEVersion % Test,
    ),
    libraryDependencies ++= Seq(
      "io.circe" %% "circe-yaml",
      "io.circe" %% "circe-core",
      "io.circe" %% "circe-generic",
      "io.circe" %% "circe-parser"
    ).map(_ % circeVersion)
  )

lazy val stt_compiler = project
  .in(file("modules/stt-compiler"))
  .settings(
    name := "stt-compiler",
    description := "State Transnition Table Compiler",
    libraryDependencies ++= Seq(
      //"org.typelevel" %% "cats-core" % catsVersion,
      //"co.fs2" %% "fs2-core" % fs2Version,
      // ("io.circe" % "circe-yaml" % "0.13.1").cross(CrossVersion.for3Use2_13),
      //"org.typelevel" %% "cats-effect" % ceVersion,
      "org.scalameta" %% "munit" % munitVersion % Test,
      "org.scalameta" %% "munit-scalacheck" % munitVersion % Test,
      "org.typelevel" %% "munit-cats-effect-3" % muniteCEVersion % Test,
      "com.novocode" % "junit-interface" % "0.11" % Test
    )
  )

lazy val protocol = project
  .in(file("modules/protocol"))
  .settings(
    name := "protocol",
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
    name := "client",
    description := "Protobuf Client",
    libraryDependencies ++= Seq(
      "io.grpc" % "grpc-netty" % grpcVersion
    ),
    scalapbCodeGeneratorOptions += CodeGeneratorOption.FlatPackage
  )
  .enablePlugins(Fs2Grpc)
  .dependsOn(protocol)
  .dependsOn(protocol % "protobuf")

lazy val server = project
  .in(file("modules/server"))
  .settings(
    scalaVersion := Scala3,
    name := "server",
    description := "Protobuf Server",
    libraryDependencies ++= List(
      "io.grpc" % "grpc-netty" % grpcVersion,
      "io.grpc" % "grpc-services" % grpcVersion
    ),
    scalapbCodeGeneratorOptions += CodeGeneratorOption.FlatPackage
  )
  .enablePlugins(Fs2Grpc)
  .dependsOn(protocol)
  .dependsOn(protocol % "protobuf")

 lazy val docs = project // new documentation project
  .in(file("./castanet-docs"))
  .settings(
    scalaVersion := Scala3,
    libraryDependencies += ("org.scalameta" %% "mdoc" % "2.2.21")
    //.withDottyCompat(scalaVersion.value)
  )
  .dependsOn(core)
  .enablePlugins(MdocPlugin) 
