import sbt._

object Dependencies {
  lazy val helenus       = "net.nmoncho"      %% "helenus-core"     % "0.8.0"
  lazy val dseJavaDriver = "com.datastax.oss"  % "java-driver-core" % "4.15.0"
  lazy val cassandraUnit = "org.cassandraunit" % "cassandra-unit"   % "4.3.1.0"
  lazy val scalaTest     = "org.scalatest"    %% "scalatest"        % "3.2.15"
  lazy val jna           = "net.java.dev.jna"  % "jna"              % "5.12.1" // Fixes M1 JNA issue
}
