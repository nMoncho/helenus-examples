package net.nmoncho.helenus.examples
package repositories

import zio._
import net.nmoncho.helenus.zio._
import models._

import java.time.LocalDate
import java.util.UUID

object ReservationRepository {

  // Q6. Find reservation by confirmation number
  def findReservationByConfirmation(
      confirmationNumber: String
  ): ZIO[ZCqlSession, CassandraException, Option[Reservation]] =
    """SELECT * FROM reservations_by_confirmation
      |WHERE confirm_number = ?""".stripMargin.toZCQL
      .prepare[String]
      .to[Reservation]
      .execute(confirmationNumber)
      .oneOption

  // Q7. Find reservations by hotel and date
  def findReservationByHotelAndDate(
      hotelId: String,
      startDate: LocalDate
  ): ZCqlStream[Reservation] =
    """SELECT * FROM reservations_by_hotel_date
      |WHERE hotel_id = ? AND start_date = ?""".stripMargin.toZCQL
      .prepare[String, LocalDate]
      .to[Reservation]
      .streamValidated(hotelId, startDate)

  // Q8. Find reservations by guest name
  def findReservationByGuestName(
      lastName: String
  ): ZIO[ZCqlSession, CassandraException, List[Reservation]] =
    """SELECT * FROM reservations_by_guest
      |WHERE guest_last_name = ?""".stripMargin.toZCQL
      .prepare[String]
      .to[Reservation]
      .execute(lastName)
      .to(List)

  // Q9. Find guest by ID
  def findGuestById(guestId: UUID): ZIO[ZCqlSession, CassandraException, Option[Guest]] =
    """SELECT * FROM guests
      |WHERE guest_id = ?""".stripMargin.toZCQL
      .prepare[UUID]
      .to[Guest]
      .execute(guestId)
      .oneOption
}
