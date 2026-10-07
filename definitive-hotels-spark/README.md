# Definitive Hotels (Spark)
This project uses the model defined in [Cassandra: The Definitive Guide](https://www.oreilly.com/library/view/cassandra-the-definitive/9781098115159/).

It is the same domain as [Definitive Hotels](../definitive-hotels), but it reads and writes
Cassandra from **Apache Spark** using the `helenus-spark` module on top of the
[spark-cassandra-connector](https://github.com/datastax/spark-cassandra-connector).

## Reads

`HotelRepository.all` / `ReservationRepository.allReservations` / `allGuests` use
`sc.cassandraTable[T]`: the connector owns the token-aware scan, while Helenus maps each
`Row` to the domain type (the frozen `address` UDT and the `addresses` map-of-UDT included)
through `helenusRowReaderFactory(T.rowMapper)`.

## Writes

`save` / `saveIfNotExists` / `saveReservations` / `saveGuests` use the CQL-first
`foreachPartitionCql` sink: a Helenus prepared statement is built once per partition and
each record is bound with compile-time arity safety. This expresses what the connector's
`saveToCassandra` cannot, e.g. the `IF NOT EXISTS` LWT in `HotelRepository.saveIfNotExists`.

## Notes

- Built for **Scala 2.13.18** with **Spark 3.5.1** and **spark-cassandra-connector 3.5.1**.
- Tests fork the JVM (only one `SparkContext` per JVM) and run against embedded Cassandra;
  `LocalSpark` holds the shared `SparkSession`.
- This example stays on the **RDD** API. The typed `Dataset` sink exists in `helenus-spark`
  but cannot run alongside `helenus-core` in one JVM: Spark SQL's Catalyst needs ANTLR
  4.9.3 while helenus-core needs 4.13.2, and there is no single version satisfying both.
- Only the connector's shaded Java driver is kept on the classpath; the unshaded
  `java-driver-core` is excluded in `build.sbt` to avoid a duplicate-class clash.
