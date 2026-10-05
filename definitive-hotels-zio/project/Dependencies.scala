import sbt.*

object Dependencies {
  lazy val helenus    = "net.nmoncho" %% "helenus-core" % "2.0.0-RC3"
  lazy val helenusZio = "net.nmoncho" %% "helenus-zio"  % "2.0.0-RC3"

  lazy val ossJavaDriver = "org.apache.cassandra"  % "java-driver-core" % "4.19.0"
  lazy val cassandraUnit = "org.cassandraunit"     % "cassandra-unit"   % "4.3.1.0"
  lazy val scalaTest     = "org.scalatest"        %% "scalatest"        % "3.2.19"
  lazy val jna           = "net.java.dev.jna"      % "jna"              % "5.18.0" // Fixes M1 JNA issue

  val zio             = "dev.zio" %% "zio"               % "2.1.21"
  val zioStreams      = "dev.zio" %% "zio-streams"       % "2.1.21"
  val zioTest         = "dev.zio" %% "zio-test"          % "2.1.21"
  val zioTestSbt      = "dev.zio" %% "zio-test-sbt"      % "2.1.21"
  val zioTestMagnolia = "dev.zio" %% "zio-test-magnolia" % "2.1.21"
}
