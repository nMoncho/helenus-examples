package net.nmoncho.helenus.examples
package repositories

import zio._
import net.nmoncho.helenus.zio._
import models._

import java.time.LocalDate

object HotelRepository {

  type HotelId    = String
  type RoomNumber = Short

  // Q1. Find hotels near given poi
  def findByPOI(poiName: String): ZIO[ZCqlSession, CassandraException, List[Hotel]] =
    """SELECT hotel_id, name, phone, address
        |FROM hotels_by_poi
        |WHERE poi_name = ?""".stripMargin.toZCQL
      .prepareAsync[String]
      .to[(String, String, String, Address)]
      .executeAsync(poiName)
      .to(List)
      .map(result =>
        result.map { case (id, name, phone, address) => Hotel.byPoi(id, name, phone, address) }
      )

  // Q2. Find information about a hotel
  def findById(id: String): ZIO[ZCqlSession, CassandraException, Option[Hotel]] =
    "SELECT * FROM hotels WHERE id = ?".toZCQL
      .prepare[String]
      .to[Hotel]
      .execute(id)
      .oneOption

  // Q3. Find pois near a hotel
  def findPOIsByHotel(id: String): ZIO[ZCqlSession, CassandraException, List[PointOfInterest]] =
    """SELECT poi_name as name, description
      |FROM pois_by_hotel
      |WHERE hotel_id = ?""".stripMargin.toZCQL
      .prepare[String]
      .to[PointOfInterest]
      .executeAsync(id)
      .to(List)

  // Q4. Find available rooms by hotel / date
  def availableRooms(id: String, date: LocalDate) =
    """SELECT room_number, is_available
      |FROM available_rooms_by_hotel_date
      |WHERE hotel_id = ? AND date = ?""".stripMargin.toZCQL
      .prepareAsync[String, LocalDate]
      .to[(Short, Boolean)]
      .execute(id, date)
      .to(Set)
      .map(_.collect {
        case (roomNumber, isAvailable) if isAvailable =>
          roomNumber
      })

  // Q5. Find amenities for a room
  def roomAmenities(
      id: String,
      roomNumber: HotelRepository.RoomNumber
  ): ZIO[ZCqlSession, CassandraException, Set[Amenity]] =
    """SELECT amenity_name as name, description
      |FROM amenities_by_room
      |WHERE hotel_id = ? AND room_number = ?""".stripMargin.toZCQL
      .prepare[String, RoomNumber]
      .to[Amenity]
      .execute(id, roomNumber)
      .to(Set)
}
