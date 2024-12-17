import Dependencies._

ThisBuild / scalaVersion := "2.13.15"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "net.nmoncho"
ThisBuild / organizationName := "nmoncho"

lazy val root = (project in file("."))
  .settings(
    name := "helenus-example-definitive-hotels-zio",
    libraryDependencies ++= Seq(
      dseJavaDriver,
      helenus,
      helenusZio,
      zio,
      zioStreams,
      cassandraUnit   % Test,
      zioTest         % Test,
      zioTestSbt      % Test,
      zioTestMagnolia % Test,
      jna             % Test
    )
  )

addCommandAlias(
  "styleFix",
  "; scalafmtSbt; scalafmtAll"
)
// See https://www.scala-sbt.org/1.x/docs/Using-Sonatype.html for instructions on how to publish to Sonatype.
