package net.nmoncho.helenus.examples.hotels.models

import java.util.UUID
import java.time.LocalDate

/** A reservation as stored in `reservations_by_hotel_date` (and, with the same
  * columns, in the `reservations_by_confirmation` materialized view).
  *
  * Field names map 1:1 to the CQL columns under
  * [[net.nmoncho.helenus.api.ColumnNamingScheme.SnakeCase]], which is what lets
  * the Table DSL use this case class as the single source of truth for the row.
  */
final case class Reservation(
    confirmNumber: String,
    hotelId: String,
    roomNumber: Short,
    guestId: UUID,
    startDate: LocalDate,
    endDate: LocalDate
)
