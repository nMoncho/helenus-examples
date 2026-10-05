package net.nmoncho.helenus.examples.models

import java.util.UUID

final case class Guest(
    id: java.util.UUID,
    firstName: String,
    lastName: String,
    title: Option[String],
    emails: Set[String],
    phoneNumbers: List[String],
    addresses: Map[String, Address],
    confirmationNumber: String
)

object Guest {
  import net.nmoncho.helenus._

  implicit val columnScheme: ColumnNamingScheme = ColumnNamingScheme.SnakeCase

  implicit val rowMapper: RowMapper[Guest] =
    RowMapper[Guest](_.id -> "guest_id", _.confirmationNumber -> "confirm_number")
}
