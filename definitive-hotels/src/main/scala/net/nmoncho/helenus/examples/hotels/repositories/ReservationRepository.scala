package net.nmoncho.helenus.examples.hotels.repositories

import net.nmoncho.helenus.CqlSessionExtension
import java.time.LocalDate
import net.nmoncho.helenus.examples.hotels.models.Reservation
import java.util.UUID
import net.nmoncho.helenus.examples.hotels.models.Guest

class ReservationRepository()(implicit session: CqlSessionExtension) {
  import net.nmoncho.helenus._

  private val queries = new ReservationRepository.Queries()

  // Q6. Find reservation by confirmation number
  def findReservationByConfirmation(confirmationNumber: String): Option[Reservation] =
    queries.reservationByConfirmation.execute(confirmationNumber).headOption

  // Q7. Find reservations by hotel and date
  def findReservationByHotelAndDate(hotelId: String, startDate: LocalDate): Set[Reservation] =
    queries.reservationByHotelDate.execute(hotelId, startDate).to(Set)

  // Q8. Find reservations by guest name
  def findReservationByGuestName(lastName: String): Set[Reservation] =
    queries.reservationByGuestName.execute(lastName).to(Set)

  // Q9. Find guest by ID
  def findGuestById(guestId: UUID): Option[Guest] =
    queries.guestById.execute(guestId).headOption

}

object ReservationRepository {

  class Queries()(implicit session: CqlSessionExtension) {
    import net.nmoncho.helenus._

    final val reservationByConfirmation =
      """SELECT * FROM reservations_by_confirmation
        |WHERE confirm_number = ?""".stripMargin.toCQL
        .prepare[String]
        .as[Reservation]

    final val reservationByHotelDate =
      """SELECT * FROM reservations_by_hotel_date
        |WHERE hotel_id = ? AND start_date = ?""".stripMargin.toCQL
        .prepare[String, LocalDate]
        .as[Reservation]

    final val reservationByGuestName =
      """SELECT * FROM reservations_by_guest
        |WHERE guest_last_name = ?""".stripMargin.toCQL
        .prepare[String]
        .as[Reservation]
    final val guestById =
      """SELECT * FROM guests
        |WHERE guest_id = ?""".stripMargin.toCQL
        .prepare[UUID]
        .as[Guest]
  }
}
