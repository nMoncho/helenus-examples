package net.nmoncho.helenus.examples

import com.datastax.oss.driver.api.core.cql._
import com.datastax.oss.driver.api.core.{
  ConsistencyLevel,
  CqlSession,
  MappedAsyncPagingIterable,
  PagingIterable
}
import net.nmoncho.helenus.ScalaBoundStatement
import net.nmoncho.helenus.api.RowMapper
import net.nmoncho.helenus.zio.{ CassandraException, ZCqlSession, ZDefaultCqlSession }
import zio.IO

import java.net.InetSocketAddress
import scala.util.Try

class ZLazyCqlSession(hostname: String, port: Int) extends ZCqlSession {

  private lazy val session = CqlSession
    .builder()
    .addContactPoint(new InetSocketAddress(hostname, port))
    .withLocalDatacenter("datacenter1")
    .build()

  private lazy val underlying = new ZDefaultCqlSession(session)

  override def close(): IO[CassandraException, Unit] =
    underlying.close()

  override def prepare(query: String): IO[CassandraException, PreparedStatement] =
    underlying.prepare(query)

  override def prepareAsync(query: String): IO[CassandraException, PreparedStatement] =
    underlying.prepareAsync(query)

  override def execute(statement: String): IO[CassandraException, ResultSet] =
    underlying.execute(statement)

  override def execute[Out: RowMapper](
      statement: ScalaBoundStatement[Out]
  ): IO[CassandraException, PagingIterable[Try[Out]]] =
    underlying.execute(statement)

  override def executeAsync(statement: String): IO[CassandraException, AsyncResultSet] =
    underlying.executeAsync(statement)

  override def executeAsync[Out: RowMapper](
      stmt: ScalaBoundStatement[Out]
  ): IO[CassandraException, MappedAsyncPagingIterable[Try[Out]]] =
    underlying.executeAsync(stmt)

  override def executeAsyncFromJava(bs: BoundStatement): IO[CassandraException, AsyncResultSet] =
    underlying.executeAsyncFromJava(bs)

  object unsafe {
    def executeFile(filename: String): Unit =
      scala.util.Using(scala.io.Source.fromResource(filename)) { src =>
        val body       = src.getLines().mkString("\n")
        val statements = body.split(";")

        statements.foreach(executeDDL)
        Thread.sleep(100) // FIXME
      }

    def executeDDL(ddl: String): ResultSet =
      try {
        session.execute(SimpleStatement.newInstance(ddl).setConsistencyLevel(ConsistencyLevel.ALL))
      } catch {
        case ex =>
          ex.printStackTrace()
          Thread.sleep(50)
          executeDDL(ddl)
      }

    def withSession(fn: CqlSession => Unit): Unit =
      fn(session)
  }

}