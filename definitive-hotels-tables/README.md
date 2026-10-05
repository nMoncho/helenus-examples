# Definitive Hotels (Tables DSL)
This project uses the model defined in [Cassandra: The Definitive Guide](https://www.oreilly.com/library/view/cassandra-the-definitive/9781098115159/).

It is the same domain as [Definitive Hotels](../definitive-hotels), but every query
and insert is expressed with the type-safe `helenus-tables` **Table DSL** instead of
raw CQL strings.

Each physical table is described once by an `object ... extends Table[A](keyspace, table)`
in [`tables.scala`](src/main/scala/net/nmoncho/helenus/examples/hotels/repositories/tables.scala):
the mapped case class `A` is the single source of truth for the schema (checked by
`registerAllColumns`), and `type PK` / `type CK` declare the keys at the type level, so
`select(...).where(... === ?)` only compiles when the predicates form a valid
primary-key restriction.

`HotelRepository` and `ReservationRepository` build their prepared statements from those
definitions; the test-data inserts (in the [test](src/test/scala/net/nmoncho/helenus/examples)
folder) use `Table.insertFrom(entity)`.

As defined in the book there are nine queries this schema tries to answer:

1. Find hotels near given poi
1. Find information about a hotel
1. Find pois near a hotel
1. Find available rooms by hotel / date
1. Find amenities for a room
1. Find reservations by confirmation number
1. Find reservations by hotel and date
1. Find reservations by guest name
1. Find guest by ID

The CQL schema still lives in [`hotels.cql`](src/main/resources/hotels.cql) and
[`reservations.cql`](src/main/resources/reservations.cql); the Table DSL is used for the
DML (reads and writes).
