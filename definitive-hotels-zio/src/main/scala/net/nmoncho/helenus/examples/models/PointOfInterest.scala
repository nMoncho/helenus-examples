package net.nmoncho.helenus.examples.models


final case class PointOfInterest(name: String, description: String)

object PointOfInterest {
  import net.nmoncho.helenus._

  implicit val rowMapper: RowMapper[PointOfInterest] = RowMapper[PointOfInterest]()
}
