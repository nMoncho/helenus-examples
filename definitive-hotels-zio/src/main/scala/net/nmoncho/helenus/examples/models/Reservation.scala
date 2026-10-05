package net.nmoncho.helenus.examples.models

import java.util.UUID
import java.time.LocalDate

final case class Reservation(
    confirmationNumber: String,
    hotelId: String,
    roomNumber: Short,
    guestId: UUID,
    startDate: LocalDate,
    endDate: LocalDate
)

object Reservation {
  import net.nmoncho.helenus._

  implicit val columnScheme: ColumnNamingScheme = ColumnNamingScheme.SnakeCase

  implicit val rowMapper: RowMapper[Reservation] =
    RowMapper[Reservation](_.confirmationNumber -> "confirm_number")
}
