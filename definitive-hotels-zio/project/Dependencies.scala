import sbt.*

object Dependencies {
  lazy val helenus    = "net.nmoncho" %% "helenus-core" % "1.8.1"
  lazy val helenusZio = "net.nmoncho" %% "helenus-zio"  % "1.8.1"

  lazy val dseJavaDriver = "com.datastax.oss"  % "java-driver-core" % "4.17.0"
  lazy val cassandraUnit = "org.cassandraunit" % "cassandra-unit"   % "4.3.1.0"
  lazy val scalaTest     = "org.scalatest"    %% "scalatest"        % "3.2.15"
  lazy val jna           = "net.java.dev.jna"  % "jna"              % "5.12.1" // Fixes M1 JNA issue

  val zio             = "dev.zio" %% "zio"               % "2.1.13"
  val zioStreams      = "dev.zio" %% "zio-streams"       % "2.1.13"
  val zioTest         = "dev.zio" %% "zio-test"          % "2.1.13"
  val zioTestSbt      = "dev.zio" %% "zio-test-sbt"      % "2.1.13"
  val zioTestMagnolia = "dev.zio" %% "zio-test-magnolia" % "2.1.13"
}
