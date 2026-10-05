import sbt._

object Dependencies {
  lazy val helenus       = "net.nmoncho"         %% "helenus-core"     % "2.0.0-RC3"
  lazy val helenusTables = "net.nmoncho"         %% "helenus-tables"   % "2.0.0-RC3"
  lazy val ossJavaDriver = "org.apache.cassandra" % "java-driver-core" % "4.19.0"
  lazy val cassandraUnit = "org.cassandraunit"    % "cassandra-unit"   % "4.3.1.0"
  lazy val scalaTest     = "org.scalatest"       %% "scalatest"        % "3.2.19"
  lazy val jna = "net.java.dev.jna" % "jna" % "5.18.0" // Fixes M1 JNA issue
}
