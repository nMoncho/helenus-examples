import sbt.*

object Dependencies {
  lazy val helenus       = "net.nmoncho"          %% "helenus-core"     % "2.0.0-RC3"
  lazy val helenusAkka   = "net.nmoncho"          %% "helenus-akka"     % "2.0.0-RC3"
  lazy val ossJavaDriver = "org.apache.cassandra"  % "java-driver-core" % "4.19.0"
  lazy val cassandraUnit = "org.cassandraunit"     % "cassandra-unit"   % "4.3.1.0"
  lazy val scalaTest     = "org.scalatest"        %% "scalatest"        % "3.2.19"
  lazy val jna           = "net.java.dev.jna"      % "jna"              % "5.18.0" // Fixes M1 JNA issue

  lazy val akkaStream  = "com.typesafe.akka"  %% "akka-stream"                   % "2.6.20"
  lazy val alpakka     = "com.lightbend.akka" %% "akka-stream-alpakka-cassandra" % "4.0.0"
  lazy val akkaTestKit = "com.typesafe.akka"  %% "akka-testkit"                  % "2.6.20"
}
