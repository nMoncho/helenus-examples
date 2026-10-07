package net.nmoncho.helenus.examples.hotels

import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession

/** A single local `SparkSession` shared across the Spark specs.
  *
  * Only one `SparkContext` can be live per JVM and `getOrCreate` hands every caller the
  * same one, so the session is created once, lazily, and left running for the JVM to
  * reclaim at exit. It points at the embedded Cassandra (started by `CassandraSpec`) via
  * the `spark.cassandra.*` keys the spark-cassandra-connector reads.
  */
object LocalSpark {

  lazy val session: SparkSession = {
    val conf = new SparkConf()
      .setMaster("local[2]")
      .setAppName("helenus-example-definitive-hotels-spark")
      .set("spark.ui.enabled", "false")
      .set("spark.driver.host", "localhost")
      .set("spark.driver.bindAddress", "localhost")
      // 127.0.0.1 (not "localhost") so the connector does not first try IPv6 ::1, which
      // the embedded Cassandra does not bind.
      .set("spark.cassandra.connection.host", "127.0.0.1")
      .set("spark.cassandra.connection.port", "9142")
      .set("spark.cassandra.connection.localDC", "datacenter1")

    SparkSession.builder().config(conf).getOrCreate()
  }
}
