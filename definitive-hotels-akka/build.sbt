import Dependencies._

ThisBuild / scalaVersion := "2.13.16"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "net.nmoncho"
ThisBuild / organizationName := "nmoncho"

lazy val root = (project in file("."))
  .settings(
    name := "helenus-example-definitive-hotels-akka",
    // Suites share the embedded Cassandra admin session (EmbeddedCassandraServerHelper.getSession),
    // whose keyspace is switched per-suite via `USE`. Running suites in parallel races on that shared
    // session, so DDL can land in the wrong keyspace. Keep suite execution sequential.
    Test / parallelExecution := false,
    libraryDependencies ++= Seq(
      helenus,
      helenusAkka,
      akkaStream,
      alpakka,
      ossJavaDriver,
      cassandraUnit % Test,
      scalaTest     % Test,
      akkaTestKit   % Test,
      jna           % Test
    )
  )

addCommandAlias(
  "styleFix",
  "; scalafmtSbt; scalafmtAll"
)
// See https://www.scala-sbt.org/1.x/docs/Using-Sonatype.html for instructions on how to publish to Sonatype.
