package net.nmoncho.helenus.examples.hotels.repositories

import net.nmoncho.helenus._
import net.nmoncho.helenus.api.ColumnNamingScheme
import net.nmoncho.helenus.api.tables._
import net.nmoncho.helenus.examples.hotels.models._

/** Table definitions for the hotels keyspace, expressed with the Helenus
  * `helenus-tables` DSL.
  *
  * Each `object ... extends Table[A](keyspace, table)` is the single source of
  * truth for one physical table: its column vals are checked references into the
  * mapped case class `A`, and `registerAllColumns` proves the case class and the
  * table never drift. `type PK` / `type CK` declare the partition and clustering
  * keys at the type level, which is what lets `select(...).where(...)` gate, at
  * compile time, whether a query is a valid primary-key restriction.
  *
  * Field names are translated to CQL column names with [[ColumnNamingScheme.SnakeCase]]
  * (e.g. `hotelId` -> `hotel_id`), matching the schema in `hotels.cql` /
  * `reservations.cql`.
  */
object Tables {

  // Q1. Find hotels near a given point of interest.
  object HotelsByPoi extends Table[HotelByPoi]("tests", "hotels_by_poi") {
    override protected def naming: ColumnNamingScheme = ColumnNamingScheme.SnakeCase

    val poiName = column[String]("poiName")
    val hotelId = column[String]("hotelId")
    val name    = column[String]("name")
    val phone   = column[String]("phone")
    val address = column[Address]("address", frozen = true)

    protected val columns = registerAllColumns(
      poiName :: hotelId :: name :: phone :: address :: HNil
    )

    type PK = poiName.Tag :: HNil
    type CK = hotelId.Tag :: HNil
  }

  // Q2. Find information about a hotel.
  object Hotels extends Table[Hotel]("tests", "hotels") {
    override protected def naming: ColumnNamingScheme = ColumnNamingScheme.SnakeCase

    val id      = column[String]("id")
    val name    = column[String]("name")
    val phone   = column[String]("phone")
    val address = column[Address]("address", frozen = true)
    val pois    = column[Set[String]]("pois")

    protected val columns = registerAllColumns(
      id :: name :: phone :: address :: pois :: HNil
    )

    type PK = id.Tag :: HNil
    type CK = HNil
  }

  // Q3. Find points of interest near a hotel.
  object PoisByHotel extends Table[PoiByHotel]("tests", "pois_by_hotel") {
    override protected def naming: ColumnNamingScheme = ColumnNamingScheme.SnakeCase

    val hotelId     = column[String]("hotelId")
    val poiName     = column[String]("poiName")
    val description = column[String]("description")

    protected val columns = registerAllColumns(
      hotelId :: poiName :: description :: HNil
    )

    type PK = hotelId.Tag :: HNil
    type CK = poiName.Tag :: HNil
  }

  // Q4. Find available rooms by hotel / date.
  object AvailableRoomsByHotelDate
      extends Table[AvailableRoom]("tests", "available_rooms_by_hotel_date") {
    override protected def naming: ColumnNamingScheme = ColumnNamingScheme.SnakeCase

    val hotelId     = column[String]("hotelId")
    val date        = column[java.time.LocalDate]("date")
    val roomNumber  = column[Short]("roomNumber")
    val isAvailable = column[Boolean]("isAvailable")

    protected val columns = registerAllColumns(
      hotelId :: date :: roomNumber :: isAvailable :: HNil
    )

    type PK = hotelId.Tag :: HNil
    type CK = date.Tag :: roomNumber.Tag :: HNil
  }

  // Q5. Find amenities for a room.
  object AmenitiesByRoom extends Table[RoomAmenity]("tests", "amenities_by_room") {
    override protected def naming: ColumnNamingScheme = ColumnNamingScheme.SnakeCase

    val hotelId     = column[String]("hotelId")
    val roomNumber  = column[Short]("roomNumber")
    val amenityName = column[String]("amenityName")
    val description = column[String]("description")

    protected val columns = registerAllColumns(
      hotelId :: roomNumber :: amenityName :: description :: HNil
    )

    type PK = hotelId.Tag :: roomNumber.Tag :: HNil
    type CK = amenityName.Tag :: HNil
  }

  // Q7. Find reservations by hotel and date.
  object ReservationsByHotelDate
      extends Table[Reservation]("tests", "reservations_by_hotel_date") {
    override protected def naming: ColumnNamingScheme = ColumnNamingScheme.SnakeCase

    val confirmNumber = column[String]("confirmNumber")
    val hotelId       = column[String]("hotelId")
    val roomNumber    = column[Short]("roomNumber")
    val guestId       = column[java.util.UUID]("guestId")
    val startDate     = column[java.time.LocalDate]("startDate")
    val endDate       = column[java.time.LocalDate]("endDate")

    protected val columns = registerAllColumns(
      confirmNumber :: hotelId :: roomNumber :: guestId :: startDate :: endDate :: HNil
    )

    type PK = hotelId.Tag :: startDate.Tag :: HNil
    type CK = roomNumber.Tag :: HNil
  }

  // Q6. Find a reservation by confirmation number (materialized view over
  // `reservations_by_hotel_date`, so the same columns, a different key).
  object ReservationsByConfirmation
      extends Table[Reservation]("tests", "reservations_by_confirmation") {
    override protected def naming: ColumnNamingScheme = ColumnNamingScheme.SnakeCase

    val confirmNumber = column[String]("confirmNumber")
    val hotelId       = column[String]("hotelId")
    val roomNumber    = column[Short]("roomNumber")
    val guestId       = column[java.util.UUID]("guestId")
    val startDate     = column[java.time.LocalDate]("startDate")
    val endDate       = column[java.time.LocalDate]("endDate")

    protected val columns = registerAllColumns(
      confirmNumber :: hotelId :: roomNumber :: guestId :: startDate :: endDate :: HNil
    )

    type PK = confirmNumber.Tag :: HNil
    type CK = hotelId.Tag :: startDate.Tag :: roomNumber.Tag :: HNil
  }

  // Q8. Find reservations by guest name.
  object ReservationsByGuest extends Table[ReservationByGuest]("tests", "reservations_by_guest") {
    override protected def naming: ColumnNamingScheme = ColumnNamingScheme.SnakeCase

    val guestLastName = column[String]("guestLastName")
    val hotelId       = column[String]("hotelId")
    val startDate     = column[java.time.LocalDate]("startDate")
    val endDate       = column[java.time.LocalDate]("endDate")
    val roomNumber    = column[Short]("roomNumber")
    val confirmNumber = column[String]("confirmNumber")
    val guestId       = column[java.util.UUID]("guestId")

    protected val columns = registerAllColumns(
      guestLastName :: hotelId :: startDate :: endDate :: roomNumber :: confirmNumber :: guestId :: HNil
    )

    type PK = guestLastName.Tag :: HNil
    type CK = hotelId.Tag :: HNil
  }

  // Q9. Find a guest by ID.
  object Guests extends Table[Guest]("tests", "guests") {
    override protected def naming: ColumnNamingScheme = ColumnNamingScheme.SnakeCase

    val guestId       = column[java.util.UUID]("guestId")
    val firstName     = column[String]("firstName")
    val lastName      = column[String]("lastName")
    val title         = column[Option[String]]("title")
    val emails        = column[Set[String]]("emails")
    val phoneNumbers  = column[List[String]]("phoneNumbers")
    val addresses     = column[Map[String, Address]]("addresses")
    val confirmNumber = column[String]("confirmNumber")

    protected val columns = registerAllColumns(
      guestId :: firstName :: lastName :: title :: emails :: phoneNumbers :: addresses :: confirmNumber :: HNil
    )

    type PK = guestId.Tag :: HNil
    type CK = HNil
  }
}
