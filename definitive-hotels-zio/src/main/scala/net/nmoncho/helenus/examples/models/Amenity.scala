package net.nmoncho.helenus.examples.models

import net.nmoncho.helenus.api.RowMapper

final case class Amenity(name: String, description: String)

object Amenity {
  import net.nmoncho.helenus._

  implicit val rowMapper: RowMapper[Amenity] = RowMapper[Amenity]
}
