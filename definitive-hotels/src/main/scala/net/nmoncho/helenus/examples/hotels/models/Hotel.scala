package net.nmoncho.helenus.examples.hotels.models

import net.nmoncho.helenus.api.RowMapper

final case class Hotel(id: String, name: String, phone: String, address: Address, pois: Set[String])

object Hotel {
  import net.nmoncho.helenus._

  implicit val rowMapper: RowMapper[Hotel] = RowMapper[Hotel]

  def byPoi(id: String, name: String, phone: String, address: Address): Hotel =
    Hotel(id, name, phone, address, Set())
}
