package net.nmoncho.helenus.examples.hotels.models

import java.util.UUID
import java.time.LocalDate
import net.nmoncho.helenus.api.RowMapper
import net.nmoncho.helenus.api.ColumnNamingScheme
import net.nmoncho.helenus.api.SnakeCase

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

  implicit val columnScheme: ColumnNamingScheme = SnakeCase

  implicit val rowMapper: RowMapper[Reservation] =
    RowMapper.NamedRowMapper[Reservation]("confirmationNumber" -> "confirm_number")
}
