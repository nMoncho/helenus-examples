package net.nmoncho.helenus.examples.hotels.repositories

import com.datastax.spark.connector._
import com.datastax.spark.connector.rdd.reader.RowReaderFactory
import net.nmoncho.helenus._
import net.nmoncho.helenus.spark._
import net.nmoncho.helenus.examples.hotels.models.Address
import net.nmoncho.helenus.examples.hotels.models.Hotel
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD

/** Spark repository for hotels.
  *
  *   - reads go through `sc.cassandraTable[Hotel]`: the spark-cassandra-connector owns the
  *     token-aware scan while Helenus maps each `Row` to `Hotel` (UDT column included) via
  *     [[helenusRowReaderFactory]];
  *   - writes go through the CQL-first [[net.nmoncho.helenus.spark.CqlSinkOps.foreachPartitionCql]]
  *     sink, which prepares a Helenus statement once per partition. This can express what
  *     the connector's `saveToCassandra` cannot, e.g. the LWT insert in [[saveIfNotExists]].
  */
object HotelRepository {

  /** The connector picks this up as the implicit `RowReaderFactory[Hotel]` at the
    * `cassandraTable[Hotel]` call site. The mapper is passed by name (a stable companion
    * `val`), so it is re-derived on the executor instead of shipping its non-serializable
    * driver codecs across the wire.
    */
  implicit val hotelReader: RowReaderFactory[Hotel] =
    helenusRowReaderFactory(Hotel.rowMapper)

  def all(sc: SparkContext): RDD[Hotel] =
    sc.cassandraTable[Hotel]("tests", "hotels")

  /** Upsert every hotel in the RDD (plain INSERT, safe to replay). */
  def save(hotels: RDD[Hotel]): Unit =
    hotels
      .map(h => (h.id, h.name, h.phone, h.address, h.pois))
      .foreachPartitionCql(
        "INSERT INTO tests.hotels(id, name, phone, address, pois) VALUES (?, ?, ?, ?, ?)"
          .toCQL(_)
          .prepare[String, String, String, Address, Set[String]]
      )

  /** Insert only hotels whose id does not already exist, via an LWT (`IF NOT EXISTS`) —
    * something the column-mapping `saveToCassandra` cannot express.
    */
  def saveIfNotExists(hotels: RDD[Hotel]): Unit =
    hotels
      .map(h => (h.id, h.name, h.phone, h.address, h.pois))
      .foreachPartitionCql(
        "INSERT INTO tests.hotels(id, name, phone, address, pois) VALUES (?, ?, ?, ?, ?) IF NOT EXISTS"
          .toCQL(_)
          .prepare[String, String, String, Address, Set[String]]
      )
}
