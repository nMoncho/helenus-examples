import sbt._

object Dependencies {
  private val sparkVersion = "3.5.1" // spark-cassandra-connector 3.5.1 targets Spark 3.5.x

  lazy val helenus      = "net.nmoncho" %% "helenus-core"  % "2.0.0"
  lazy val helenusSpark = "net.nmoncho" %% "helenus-spark" % "2.0.0"

  lazy val sparkCore = "org.apache.spark" %% "spark-core" % sparkVersion
  lazy val sparkSql  = "org.apache.spark" %% "spark-sql"  % sparkVersion
  lazy val sparkCassandraConnector =
    "com.datastax.spark" %% "spark-cassandra-connector" % sparkVersion

  lazy val cassandraUnit = "org.cassandraunit" % "cassandra-unit" % "4.3.1.0"
  lazy val scalaTest     = "org.scalatest"    %% "scalatest"       % "3.2.19"
  lazy val jna = "net.java.dev.jna" % "jna" % "5.18.0" // Fixes M1 JNA issue
}
