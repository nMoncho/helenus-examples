package net.nmoncho.helenus.examples.hotels.models

import java.util.UUID

/** A guest as stored in the `guests` table. Field names map 1:1 to the CQL
  * columns under
  * [[net.nmoncho.helenus.api.ColumnNamingScheme.SnakeCase]] (`guestId` ->
  * `guest_id`, `confirmNumber` -> `confirm_number`), so the Table DSL can derive
  * the whole schema from the case class.
  */
final case class Guest(
    guestId: UUID,
    firstName: String,
    lastName: String,
    title: Option[String],
    emails: Set[String],
    phoneNumbers: List[String],
    addresses: Map[String, Address],
    confirmNumber: String
)
