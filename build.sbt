val Scala3   = "3.1.1"
val Scala213 = "2.13.8"

val catsVersion          = "2.7.0"
val ceVersion            = "3.3.5"
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
ThisBuild / scalaVersion         := Scala3
ThisBuild / version := "0.1.4"

ThisBuild / organization := "ee.mn8"
ThisBuild / organizationName := "MN8 Technology (Pty) Ltd"
ThisBuild / organizationHomepage := Some(url("https://mn8.tech"))

ThisBuild / scmInfo := Some(
  ScmInfo(
    url("https://github.com/username/project"),
    "scm:git@github.username/project.git"
  )
)

ThisBuild / developers := List(
  Developer(
    id    = "iandebeer",
    name  = "Ian de Beer",
    email = "ian@mn8.ee",
    url   = url("https://mn8.tech")
  )
)

ThisBuild / description := "Coloured Petri fo Scala 3"
ThisBuild / licenses := List("MIT License" -> new URL("https://tldrlegal.com/license/mit-license#summary"))
ThisBuild / homepage := Some(url("https://github.com/username/project"))

// Remove all additional repository other than Maven Central from POM
ThisBuild / pomIncludeRepository := { _ => false }

ThisBuild / publishTo := {
  val nexus = "https://s01.oss.sonatype.org/"
  if (isSnapshot.value) Some("snapshots" at nexus + "content/repositories/snapshots")
  else Some("releases" at nexus + "service/local/staging/deploy/maven2")
}

ThisBuild / publishMavenStyle := true

ThisBuild / versionScheme := Some("early-semver")

lazy val root = project
  .in(file("."))
  .aggregate(core)
  .settings(
    publish / skip := true
  )

lazy val core = project
  .in(file("modules/core"))
  .settings(
    name := "castanet",
    resolvers += "Local Maven Repository" at "file://" + Path.userHome.absolutePath + "/.m2/repository",
    libraryDependencies ++= Seq(
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


