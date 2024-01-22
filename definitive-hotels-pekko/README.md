# Definitive Hotels
This project uses the model defined [Cassandra: The Definitive Guide](https://www.oreilly.com/library/view/cassandra-the-definitive/9781098115159/).

This project defines two repositories, `HotelRepository` and `ReservationRepository`
to query information about hotels and reservations. You can read the [`hotels.cql`](src/main/resources/hotels.cql) and [`reservations.cql`](src/main/resources/reservations.cql).

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

You can see how queries are defined in their respective repositories. `INSERT` 
statements are defined the [test](src/test/scala/net/nmoncho/helenus/examples) folder.

## Async operations
This repository shows one way to use the _asynchronous_ operations available on Helenus.
Part of this is present on the repositories, and another part is on the tests, be sure
to check both.
