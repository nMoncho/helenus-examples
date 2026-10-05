package net.nmoncho.helenus.examples.hotels.models

import java.util.UUID
import java.time.LocalDate

/** A row of `reservations_by_guest` (Q8): reservations denormalized by the
  * guest's last name.
  */
final case class ReservationByGuest(
    guestLastName: String,
    hotelId: String,
    startDate: LocalDate,
    endDate: LocalDate,
    roomNumber: Short,
    confirmNumber: String,
    guestId: UUID
)
