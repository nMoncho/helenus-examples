import Dependencies._

// helenus-spark (and its Spark 3.5.1 deps) are published for scala-library 2.13.18.
ThisBuild / scalaVersion := "2.13.18"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "net.nmoncho"
ThisBuild / organizationName := "nmoncho"

lazy val root = (project in file("."))
  .settings(
    name := "helenus-example-definitive-hotels-spark",
    libraryDependencies ++= Seq(
      helenus,
      helenusSpark,
      sparkCore,
      sparkSql,
      sparkCassandraConnector,
      cassandraUnit % Test,
      scalaTest     % Test,
      jna           % Test
    ),
    // helenus-core pulls the *unshaded* `org.apache.cassandra:java-driver-core`, while the
    // spark-cassandra-connector bundles a *shaded* copy that exposes the same
    // `com.datastax.oss.driver.api.core.*` API. Having both is a LinkageError risk, so we
    // converge on the connector's shaded driver and drop the unshaded one (mirrors the
    // helenus-spark module's own build).
    excludeDependencies ++= Seq(
      ExclusionRule("org.apache.cassandra", "java-driver-core"),
      ExclusionRule("org.apache.cassandra", "java-driver-guava-shaded")
    ),
    // Spark runs in a single embedded context, and only one SparkContext may be live per
    // JVM, so fork the test JVM and keep suites sequential.
    Test / fork := true,
    Test / parallelExecution := false,
    // Spark 3.5 on JDK 11+ needs these module-access grants for its (de)serialization paths.
    Test / javaOptions ++= Seq(
      "--add-opens=java.base/java.lang=ALL-UNNAMED",
      "--add-opens=java.base/java.lang.invoke=ALL-UNNAMED",
      "--add-opens=java.base/java.io=ALL-UNNAMED",
      "--add-opens=java.base/java.net=ALL-UNNAMED",
      "--add-opens=java.base/java.nio=ALL-UNNAMED",
      "--add-opens=java.base/java.util=ALL-UNNAMED",
      "--add-opens=java.base/java.util.concurrent=ALL-UNNAMED",
      "--add-opens=java.base/sun.nio.ch=ALL-UNNAMED",
      "--add-opens=java.base/sun.security.action=ALL-UNNAMED"
    )
  )

addCommandAlias(
  "styleFix",
  "; scalafmtSbt; scalafmtAll"
)
// See https://www.scala-sbt.org/1.x/docs/Using-Sonatype.html for instructions on how to publish to Sonatype.
