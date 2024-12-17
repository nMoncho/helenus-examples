package net.nmoncho.helenus.examples

import com.datastax.oss.driver.api.core.CqlSession
import com.datastax.oss.driver.api.core.cql.ResultSet
import net.nmoncho.helenus.zio.{ ZCqlSession, ZDefaultCqlSession }
import org.cassandraunit.utils.EmbeddedCassandraServerHelper
import zio.{ Task, ZIO, ZLayer }
import zio.test.ZIOSpec

import java.util.UUID

abstract class ZCassandraSpec extends ZIOSpec[ZCqlSession] {

  protected lazy val keyspace: String = randomIdentifier("tests")

  private val hostname   = "localhost"
  private val port       = 9142
  private val underlying = new ZLazyCqlSession(hostname, port)

  protected val contactPoint: String = s"$hostname:$port"

  private def startTimeout: Long = EmbeddedCassandraServerHelper.DEFAULT_STARTUP_TIMEOUT * 3

  override val bootstrap: ZLayer[Any, Throwable, ZCqlSession] =
    ZLayer.scoped(
      ZIO.attempt(EmbeddedCassandraServerHelper.startEmbeddedCassandra(startTimeout)) *>
        ZIO.attempt(underlying)
    )

  def execute(statement: String): ZIO[ZCqlSession, Throwable, ResultSet] =
    ZIO.service[ZCqlSession].flatMap(_.execute(statement))

  def randomIdentifier(prefix: String): String =
    s"${prefix}_${UUID.randomUUID().toString}".replaceAll("-", "_")

  def executeFile(filename: String): Unit =
    underlying.unsafe.executeFile(filename)

  def withSession(fn: CqlSession => Unit): Unit =
    underlying.unsafe.withSession(fn)

  protected def createKeyspace(): ZIO[ZCqlSession, Throwable, Unit] = for {
    _ <- execute(
      s"CREATE KEYSPACE IF NOT EXISTS $keyspace WITH replication = {'class':'SimpleStrategy', 'replication_factor' : 1}"
    )
    _ <- execute(s"USE $keyspace")
  } yield ()

  protected def executeDDL(ddl: String): Task[Unit] =
    ZIO.attempt(underlying.unsafe.executeDDL(ddl)).map(_ => ())

  def executeFileDDL(filename: String): Task[Unit] =
    ZIO.attempt(underlying.unsafe.executeFile(filename)).map(_ => ())
}
