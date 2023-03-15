val Scala3   = "3.2.1"
val Scala213 = "2.13.8"

val catsVersion          = "2.8.0"
val ceVersion            = "3.3.14"
val fs2Version           = "3.2.10"
val munitVersion         = "1.0.0-M6"
val munitCEVersion       = "1.0.7"
val munitCheckEffVersion = "1.0.0-M1"
val googleProtoVersion   = "3.19.1"
val circeVersion         = "0.14.2"
val monocleVersion       = "3.1.0"
val scodecVersion        = "1.1.34"
val junitVersion         = "0.11"
val refinedVersion       = "0.9.27"

Global / onChangedBuildSource := ReloadOnSourceChanges
ThisBuild / scalaVersion      := Scala3
ThisBuild / version           := "0.1.5"

ThisBuild / organization         := "dev.mn8"
ThisBuild / organizationName     := "MN8 Technology ÖU"
ThisBuild / organizationHomepage := Some(url("https://mn8.dev"))

ThisBuild / scmInfo := Some(
  ScmInfo(
    url("https://github.com/iandebeer/castanet"),
    "scm:git@github.iandebeer/castanet.git"
  )
)

ThisBuild / developers := List(
  Developer(
    id = "iandebeer",
    name = "Ian de Beer",
    email = "ian@mn8.ee",
    url = url("https://mn8.dev")
  )
)

ThisBuild / description := "Coloured Petri fo Scala 3"
ThisBuild / licenses := List(
  "MIT License" -> new URL("https://tldrlegal.com/license/mit-license#summary")
)
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
    publish / skip            := true,
    publishConfiguration      := publishConfiguration.value.withOverwrite(true),
    publishLocalConfiguration := publishLocalConfiguration.value.withOverwrite(true)
  )

lazy val core = project
  .in(file("modules/core"))
  .settings(
    name                      := "castanet",
    publishConfiguration      := publishConfiguration.value.withOverwrite(true),
    publishLocalConfiguration := publishLocalConfiguration.value.withOverwrite(true),
    resolvers += "Local Maven Repository" at "file://" + Path.userHome.absolutePath + "/.m2/repository",
    libraryDependencies ++= Seq(
      "org.typelevel"  %% "cats-core"           % catsVersion,
      "co.fs2"         %% "fs2-core"            % fs2Version,
      "co.fs2"         %% "fs2-io"              % fs2Version,
      "org.typelevel"  %% "cats-effect"         % ceVersion,
      "dev.optics"     %% "monocle-core"        % monocleVersion,
      "org.scodec"     %% "scodec-bits"         % scodecVersion,
      "org.scala-lang" %% "scala3-staging"      % Scala3,
      "io.circe" %% "circe-yaml"                % "0.14.1",
      "org.scalameta"  %% "munit"               % munitVersion   % Test,
      "org.scalameta"  %% "munit-scalacheck"    % munitVersion   % Test,
      "org.typelevel"  %% "munit-cats-effect-3" % munitCEVersion % Test
    ),
    libraryDependencies ++= Seq(
     // "io.circe" %% "circe-yaml",
      "io.circe" %% "circe-core",
      "io.circe" %% "circe-generic",
      "io.circe" %% "circe-parser"
    ).map(_ % circeVersion)
  )
