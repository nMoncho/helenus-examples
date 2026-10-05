package net.nmoncho.helenus.examples.hotels.repositories

import com.datastax.oss.driver.api.core.CqlSession
import net.nmoncho.helenus.examples.hotels.models.Guest
import net.nmoncho.helenus.examples.hotels.models.Reservation
import java.time.LocalDate
import java.util.UUID

/** Reservation queries expressed with the `helenus-tables` DSL. See
  * [[HotelRepository]] for the general shape; here `reservationByConfirmation`
  * queries a materialized view and `reservationByGuestName` a separate
  * denormalized table, both declared in [[Tables]].
  */
class ReservationRepository()(implicit session: CqlSession) {
  import net.nmoncho.helenus._

  private val queries = new ReservationRepository.Queries()

  // Q6. Find reservation by confirmation number
  def findReservationByConfirmation(confirmNumber: String): Option[Reservation] =
    queries.reservationByConfirmation.execute(confirmNumber).nextOption

  // Q7. Find reservations by hotel and date
  def findReservationByHotelAndDate(hotelId: String, startDate: LocalDate): Set[Reservation] =
    queries.reservationByHotelDate.execute(hotelId, startDate).to(Set)

  // Q8. Find reservations by guest name
  def findReservationByGuestName(lastName: String): Set[Reservation] =
    queries.reservationByGuestName
      .execute(lastName)
      .to(Set)
      .map(r =>
        Reservation(r.confirmNumber, r.hotelId, r.roomNumber, r.guestId, r.startDate, r.endDate)
      )

  // Q9. Find guest by ID
  def findGuestById(guestId: UUID): Option[Guest] =
    queries.guestById.execute(guestId).nextOption

}

object ReservationRepository {

  class Queries()(implicit session: CqlSession) {
    import net.nmoncho.helenus._
    import net.nmoncho.helenus.api.tables._
    import Tables._

    final val reservationByConfirmation =
      ReservationsByConfirmation
        .select()
        .where(ReservationsByConfirmation.confirmNumber === ?)
        .prepare

    final val reservationByHotelDate =
      ReservationsByHotelDate
        .select()
        .where(ReservationsByHotelDate.hotelId === ? and ReservationsByHotelDate.startDate === ?)
        .prepare

    final val reservationByGuestName =
      ReservationsByGuest.select().where(ReservationsByGuest.guestLastName === ?).prepare

    final val guestById =
      Guests.select().where(Guests.guestId === ?).prepare
  }
}
