package net.nmoncho.helenus.examples.hotels.repositories

import net.nmoncho.helenus.examples.hotels.models.{ Guest, Reservation }
import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.alpakka.cassandra.scaladsl.CassandraSession
import akka.stream.scaladsl.{ Sink, Source }

import java.time.LocalDate
import java.util.UUID
import scala.concurrent.{ ExecutionContext, Future }

class ReservationRepository()(implicit system: ActorSystem, session: CassandraSession) {

  import system.dispatcher
  import net.nmoncho.helenus._
  import net.nmoncho.helenus.akka._

  private val queries = new ReservationRepository.Queries()

  // Q6. Find reservation by confirmation number
  def findReservationByConfirmation(confirmationNumber: String): Future[Option[Reservation]] =
    // This mixes both async and sync API, if you desire to do so
    queries.reservationByConfirmation
      .asReadSource(confirmationNumber)
      .runWith(Sink.headOption)

  // Q7. Find reservations by hotel and date
  def findReservationByHotelAndDate(
      hotelId: String,
      startDate: LocalDate
  ): Source[Reservation, NotUsed] =
    queries.reservationByHotelDate.asReadSource(hotelId, startDate)

  // Q8. Find reservations by guest name
  def findReservationByGuestName(lastName: String): Source[Reservation, NotUsed] =
    queries.reservationByGuestName.asReadSource(lastName)

  // Q9. Find guest by ID
  def findGuestById(guestId: UUID): Future[Option[Guest]] =
    queries.guestById.asReadSource(guestId).runWith(Sink.headOption)

}

object ReservationRepository {

  class Queries()(implicit session: CassandraSession, ec: ExecutionContext) {
    import net.nmoncho.helenus._
    import net.nmoncho.helenus.akka._

    final val reservationByConfirmation =
      """SELECT * FROM reservations_by_confirmation
        |WHERE confirm_number = ?""".stripMargin.toCQLAsync
        .prepare[String]
        .as[Reservation]

    final val reservationByHotelDate =
      """SELECT * FROM reservations_by_hotel_date
        |WHERE hotel_id = ? AND start_date = ?""".stripMargin.toCQLAsync
        .prepare[String, LocalDate]
        .as[Reservation]

    final val reservationByGuestName =
      """SELECT * FROM reservations_by_guest
        |WHERE guest_last_name = ?""".stripMargin.toCQLAsync
        .prepare[String]
        .as[Reservation]

    final val guestById =
      """SELECT * FROM guests
        |WHERE guest_id = ?""".stripMargin.toCQLAsync
        .prepare[UUID]
        .as[Guest]
  }
}
