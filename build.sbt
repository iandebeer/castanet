import sbt.Keys.libraryDependencies

val Scala3 = "3.0.0-RC1"
val Scala213 = "2.13.5"

val catsVersion = "2.4.2"
val ceVersion = "3.0.0-RC3"//"2.3.3"
val fs2Version = "2.5.3"
val munitVersion = "0.7.22"
val muniteCEVersion = "0.13.1"
val munitCheckEffVersion = "0.7.1"


ThisBuild / initialCommands := """
  |import cats._, data._, syntax.all._
  |import cats.effect._, concurrent._, cats.effect.implicits._
  |import fs2._
  |import fs2.concurrent._
  |import scala.concurrent.duration._
  |import ee.mn8.castanet._
""".stripMargin


def dep(org: String, prefix: String, version: String)(modules: String*)(testModules: String*) =
  modules.map(m => org %% (prefix ++ m) % version) ++
   testModules.map(m => org %% (prefix ++ m) % version % Test)

lazy val root = project
  .in(file("."))
  .aggregate(core,protocol,stt_compiler,docs)

lazy val core = project
  .in(file("modules/core"))
  .settings(
    scalaVersion := Scala3,
    name := "core",
 libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-core" % catsVersion,
      "co.fs2" %% "fs2-core" % fs2Version,
      //"org.typelevel" %% "cats-effect-std" % ceVersion,
      "org.typelevel" %% "cats-effect"     % ceVersion,
      "org.scalameta" %% "munit" % munitVersion % Test,
      "org.typelevel" %%  "munit-cats-effect-3" %  muniteCEVersion % Test,
      "com.novocode" % "junit-interface" % "0.11" %  Test))

lazy val stt_compiler = project
  .in(file("modules/stt-compiler"))
  .settings(
    scalaVersion := Scala3,
    name := "stt-compiler",
    description := "State Transnition Table Compiler",
  libraryDependencies ++= Seq(
       "org.typelevel" %% "cats-core" % catsVersion,
      "co.fs2" %% "fs2-core" % fs2Version,
      //"org.typelevel" %% "cats-effect-std" % ceVersion,
      "org.typelevel" %% "cats-effect"     % ceVersion,
      "org.scalameta" %% "munit" % munitVersion % Test,
      "org.typelevel" %%  "munit-cats-effect-3" %  muniteCEVersion % Test,
      "com.novocode" % "junit-interface" % "0.11" %  Test))

lazy val protocol = project
  .in(file("modules/protocol"))
  .settings(
    scalaVersion := Scala3,
    name := "protocol",
    description := "Protobuf definitions",
    Compile / PB.targets := Seq(
      scalapb.gen() -> (Compile / sourceManaged).value / "scalapb"
    ),
    libraryDependencies ++= Seq(
      "io.grpc" % "grpc-netty" % scalapb.compiler.Version.grpcJavaVersion,
      "com.thesamet.scalapb" %% "scalapb-runtime-grpc" % scalapb.compiler.Version.scalapbVersion,
      "com.thesamet.scalapb" %% "scalapb-runtime" % scalapb.compiler.Version.scalapbVersion % "protobuf"
    )
  )

lazy val docs = project       // new documentation project
  .in(file("./castanet-docs")) 
  .settings(
    scalacOptions ++= Seq(
    "-deprecation",
    "-encoding", "UTF-8",
    "-feature",
    "-unchecked",
    "-language:higherKinds",
    "-language:implicitConversions",
    "-Xfatal-warnings",
    "-Xlint",
    "-Ytasty-reader"),
    scalaVersion := Scala213,
    libraryDependencies += ("org.scalameta" %% "mdoc" % "2.2.18").withDottyCompat(scalaVersion.value)
      )
  .dependsOn(core,stt_compiler)
  .enablePlugins(MdocPlugin)