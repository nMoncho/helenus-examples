package net.nmoncho.helenus.examples.hotels.repositories

import net.nmoncho.helenus.examples.hotels.models.Hotel
import net.nmoncho.helenus.CqlSessionExtension
import net.nmoncho.helenus.examples.hotels.models.Amenity
import net.nmoncho.helenus.examples.hotels.models.Address
import net.nmoncho.helenus.examples.hotels.models.PointOfInterest
import java.time.LocalDate

class HotelRepository()(implicit session: CqlSessionExtension) {
  import net.nmoncho.helenus._

  private val queries = new HotelRepository.Queries()

  // Q1. Find hotels near given poi
  def findByPOI(poiName: String): Seq[Hotel] =
    queries.byPoi
      .execute(poiName)
      .to(List)
      .map { case (id, name, phone, address) =>
        Hotel.byPoi(id, name, phone, address)
      }

  // Q2. Find information about a hotel
  def findById(id: String): Option[Hotel] =
    queries.byId.execute(id).headOption

  // Q3. Find pois near a hotel
  def findPOIsByHotel(id: String): Seq[PointOfInterest] =
    queries.poiByHotel.execute(id).to(List)

  // Q4. Find available rooms by hotel / date
  def availableRooms(id: String, date: LocalDate): Set[HotelRepository.RoomNumber] =
    queries.availableRoomsByHotel.execute(id, date).to(Set).collect {
      case (roomNumber, isAvailable) if isAvailable =>
        roomNumber
    }

  // Q5. Find amenities for a room
  def roomAmenities(id: String, roomNumber: HotelRepository.RoomNumber): Set[Amenity] =
    queries.roomAmenities.execute(id, roomNumber).to(Set)
}

object HotelRepository {

  type HotelId    = String
  type RoomNumber = Short

  class Queries()(implicit session: CqlSessionExtension) {
    import net.nmoncho.helenus._

    final val byPoi =
      """SELECT hotel_id, name, phone, address
        |FROM hotels_by_poi
        |WHERE poi_name = ?""".stripMargin.toCQL
        .prepare[String]
        .as[(String, String, String, Address)]

    final val byId =
      "SELECT * FROM hotels WHERE id = ?".toCQL
        .prepare[String]
        .as[Hotel]

    final val poiByHotel =
      """SELECT poi_name as name, description
        |FROM pois_by_hotel
        |WHERE hotel_id = ?""".stripMargin.toCQL
        .prepare[String]
        .as[PointOfInterest]

    final val availableRoomsByHotel =
      """SELECT room_number, is_available
        |FROM available_rooms_by_hotel_date
        |WHERE hotel_id = ? AND date = ?""".stripMargin.toCQL
        .prepare[String, LocalDate]
        .as[(Short, Boolean)]

    final val roomAmenities =
      """SELECT amenity_name as name, description
        |FROM amenities_by_room
        |WHERE hotel_id = ? AND room_number = ?""".stripMargin.toCQL
        .prepare[String, RoomNumber]
        .as[Amenity]

  }
}
