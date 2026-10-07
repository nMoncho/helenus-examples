package net.nmoncho.helenus.examples.hotels.repositories

import com.datastax.spark.connector._
import com.datastax.spark.connector.rdd.reader.RowReaderFactory
import net.nmoncho.helenus._
import net.nmoncho.helenus.spark._
import net.nmoncho.helenus.examples.hotels.models.Address
import net.nmoncho.helenus.examples.hotels.models.Guest
import net.nmoncho.helenus.examples.hotels.models.Reservation
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD
import java.time.LocalDate
import java.util.UUID

/** Spark repository for reservations and guests: full-table reads through
  * `sc.cassandraTable[T]` (mapped by the Helenus `RowMapper`, which handles the renamed
  * columns such as `confirm_number` and the `addresses` map-of-UDT), and CQL-first bulk
  * writes through [[net.nmoncho.helenus.spark.CqlSinkOps.foreachPartitionCql]].
  */
object ReservationRepository {

  implicit val reservationReader: RowReaderFactory[Reservation] =
    helenusRowReaderFactory(Reservation.rowMapper)

  implicit val guestReader: RowReaderFactory[Guest] =
    helenusRowReaderFactory(Guest.rowMapper)

  def allReservations(sc: SparkContext): RDD[Reservation] =
    sc.cassandraTable[Reservation]("tests", "reservations_by_hotel_date")

  def allGuests(sc: SparkContext): RDD[Guest] =
    sc.cassandraTable[Guest]("tests", "guests")

  def saveReservations(reservations: RDD[Reservation]): Unit =
    reservations
      .map(r => (r.hotelId, r.startDate, r.endDate, r.roomNumber, r.confirmationNumber, r.guestId))
      .foreachPartitionCql(
        """INSERT INTO tests.reservations_by_hotel_date
          |(hotel_id, start_date, end_date, room_number, confirm_number, guest_id)
          |VALUES (?, ?, ?, ?, ?, ?)""".stripMargin
          .toCQL(_)
          .prepare[String, LocalDate, LocalDate, Short, String, UUID]
      )

  def saveGuests(guests: RDD[Guest]): Unit =
    guests
      .map(g =>
        (
          g.id,
          g.firstName,
          g.lastName,
          g.title,
          g.emails,
          g.phoneNumbers,
          g.addresses,
          g.confirmationNumber
        )
      )
      .foreachPartitionCql(
        """INSERT INTO tests.guests
          |(guest_id, first_name, last_name, title, emails, phone_numbers, addresses, confirm_number)
          |VALUES (?, ?, ?, ?, ?, ?, ?, ?)""".stripMargin
          .toCQL(_)
          .prepare[UUID, String, String, Option[String], Set[String], List[String], Map[
            String,
            Address
          ], String]
      )
}
