package net.nmoncho.helenus.examples.hotels.repositories

import com.datastax.oss.driver.api.core.CqlSession
import com.datastax.oss.driver.api.core.`type`.codec.TypeCodec
import com.datastax.oss.driver.api.core.`type`.codec.registry.MutableCodecRegistry
import com.datastax.oss.driver.api.core.cql.{ ResultSet, Statement }
import org.apache.cassandra.config.DatabaseDescriptor
import org.cassandraunit.utils.EmbeddedCassandraServerHelper
import org.scalatest.{ BeforeAndAfterAll, BeforeAndAfterEach, Suite }

import java.net.InetSocketAddress
import java.util.UUID
import javax.management.InstanceAlreadyExistsException

trait CassandraSpec extends BeforeAndAfterAll with BeforeAndAfterEach { this: Suite =>

  def startTimeout: Long = EmbeddedCassandraServerHelper.DEFAULT_STARTUP_TIMEOUT * 3

  protected lazy val keyspace: String = randomIdentifier("tests")

  protected lazy val contactPoint: String =
    s"${EmbeddedCassandraServerHelper.getHost}:${EmbeddedCassandraServerHelper.getNativeTransportPort}"

  protected lazy val session: CqlSession = CqlSession
    .builder()
    .addContactPoint(
      new InetSocketAddress(
        EmbeddedCassandraServerHelper.getHost,
        EmbeddedCassandraServerHelper.getNativeTransportPort
      )
    )
    .withLocalDatacenter("datacenter1")
    .withKeyspace(keyspace)
    .build()

  override def beforeAll(): Unit = {
    super.beforeAll()
    // Must have only one instance of Cassandra
    Runtime.getRuntime.synchronized {
      // Cross-test tries to start Cassandra twice, but the objects are different
      if (Option(System.getProperty("cassandra-foreground")).isEmpty) {
        // Start embedded cassandra normally
        EmbeddedCassandraServerHelper.startEmbeddedCassandra(startTimeout)
      } else {
        try {
          // Use reflection to load the config into `DatabaseDescriptor`
          val config = DatabaseDescriptor.loadConfig()
          val m = classOf[DatabaseDescriptor].getDeclaredMethod(
            "setConfig",
            classOf[org.apache.cassandra.config.Config]
          )
          m.setAccessible(true)

          m.invoke(null, config)
          DatabaseDescriptor.applyAddressConfig()
        } catch {
          case e: RuntimeException
              if e.getCause.isInstanceOf[InstanceAlreadyExistsException] => // ignore
        }
      }

      val session = EmbeddedCassandraServerHelper.getSession
      session.execute(
        s"CREATE KEYSPACE IF NOT EXISTS $keyspace WITH replication = {'class':'SimpleStrategy', 'replication_factor' : 1}"
      )
      session.execute(s"USE $keyspace")
    }
  }

  def randomIdentifier(prefix: String): String =
    s"${prefix}_${UUID.randomUUID().toString}".replaceAll("-", "_")

  def execute(query: String): ResultSet =
    session.execute(query)

  /** Executes a DDL statement until success.
    *
    * This is a workaround for the exception `DriverTimeoutException: Query timed out after PT2S`
    */
  def executeDDL(ddl: String): Unit = try {
    session.execute(ddl)
  } catch {
    case _: Throwable =>
      Thread.sleep(50)
      executeDDL(ddl)
  }

  def execute(stmt: Statement[_]): ResultSet =
    session.execute(stmt)

  def executeFile(filename: String): Unit =
    scala.util.Using(scala.io.Source.fromResource(filename)) { src =>
      val body       = src.getLines().mkString("\n")
      val statements = body.split(";")

      statements.foreach(executeDDL)
    }

  def registerCodec[T](codec: TypeCodec[T]): Unit =
    session.getContext.getCodecRegistry.asInstanceOf[MutableCodecRegistry].register(codec)

  def truncateKeyspace(keyspace: String): Unit = {
    import scala.jdk.CollectionConverters._
    val session = EmbeddedCassandraServerHelper.getSession
    val rs = session
      .execute(s"SELECT table_name FROM system_schema.tables WHERE keyspace_name = '$keyspace'")
      .asScala

    rs.foreach(row => session.execute(s"TRUNCATE TABLE ${keyspace}.${row.getString(0)}"))
  }
}
