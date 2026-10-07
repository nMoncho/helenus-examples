import sbt._

object Dependencies {
  lazy val helenus       = "net.nmoncho"          %% "helenus-core"     % "2.0.0"
  lazy val helenusPekko  = "net.nmoncho"          %% "helenus-pekko"    % "2.0.0"
  lazy val ossJavaDriver = "org.apache.cassandra"  % "java-driver-core" % "4.19.0"
  lazy val cassandraUnit = "org.cassandraunit"     % "cassandra-unit"   % "4.3.1.0"
  lazy val scalaTest     = "org.scalatest"        %% "scalatest"        % "3.2.19"
  lazy val jna           = "net.java.dev.jna"      % "jna"              % "5.18.0" // Fixes M1 JNA issue

  lazy val pekkoStream    = "org.apache.pekko" %% "pekko-stream"               % "1.2.1"
  lazy val pekkoConnector = "org.apache.pekko" %% "pekko-connectors-cassandra" % "1.2.0"
  lazy val pekkoTestKit   = "org.apache.pekko" %% "pekko-testkit"              % "1.2.1"
}
