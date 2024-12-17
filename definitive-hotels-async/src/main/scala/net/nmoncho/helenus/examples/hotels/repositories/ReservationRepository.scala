package net.nmoncho.helenus.examples.hotels.repositories

import java.time.LocalDate
import net.nmoncho.helenus.examples.hotels.models.Reservation
import java.util.UUID
import net.nmoncho.helenus.examples.hotels.models.Guest
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.concurrent.duration.DurationInt
import com.datastax.oss.driver.api.core.CqlSession

class ReservationRepository()(implicit session: CqlSession, ec: ExecutionContext) {
  import net.nmoncho.helenus._

  private val queries = new ReservationRepository.Queries()

  // Q6. Find reservation by confirmation number
  def findReservationByConfirmation(confirmationNumber: String): Future[Option[Reservation]] =
    // This mixes both async and sync API, if you desire to do so
    queries.reservationByConfirmation
      .map(_.execute(confirmationNumber).nextOption)

  // Q7. Find reservations by hotel and date
  def findReservationByHotelAndDate(
      hotelId: String,
      startDate: LocalDate
  ): Future[Set[Reservation]] = for {
    q <- queries.reservationByHotelDate
    result <- q.executeAsync(hotelId, startDate)
  } yield result.iter(5.seconds).toSet

  // Q8. Find reservations by guest name
  def findReservationByGuestName(lastName: String): Future[Set[Reservation]] = for {
    q <- queries.reservationByGuestName
    result <- q.executeAsync(lastName)
    reservations <- fetchAllPages(result.currPage, result)
  } yield reservations.toSet

  // Q9. Find guest by ID
  def findGuestById(guestId: UUID): Future[Option[Guest]] = for {
    q <- queries.guestById
    result <- q.executeAsync(guestId)
  } yield result.currPage.nextOption

}

object ReservationRepository {

  class Queries()(implicit session: CqlSession, ec: ExecutionContext) {
    import net.nmoncho.helenus._

    final val reservationByConfirmation =
      """SELECT * FROM reservations_by_confirmation
        |WHERE confirm_number = ?""".stripMargin.toCQL
        .prepareAsync[String]
        .as[Reservation]

    final val reservationByHotelDate =
      """SELECT * FROM reservations_by_hotel_date
        |WHERE hotel_id = ? AND start_date = ?""".stripMargin.toCQL
        .prepareAsync[String, LocalDate]
        .as[Reservation]

    final val reservationByGuestName =
      """SELECT * FROM reservations_by_guest
        |WHERE guest_last_name = ?""".stripMargin.toCQL
        .prepareAsync[String]
        .as[Reservation]

    final val guestById =
      """SELECT * FROM guests
        |WHERE guest_id = ?""".stripMargin.toCQL
        .prepareAsync[UUID]
        .as[Guest]
  }
}
