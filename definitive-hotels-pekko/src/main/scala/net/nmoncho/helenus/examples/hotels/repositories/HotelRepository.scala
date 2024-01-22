package net.nmoncho.helenus.examples.hotels.repositories

import net.nmoncho.helenus.examples.hotels.models.{ Address, Amenity, Hotel, PointOfInterest }
import org.apache.pekko.NotUsed
import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.stream.connectors.cassandra.scaladsl.CassandraSession
import org.apache.pekko.stream.scaladsl.{ Sink, Source }

import java.time.LocalDate
import scala.concurrent.{ ExecutionContext, Future }
import scala.concurrent.duration.DurationInt

class HotelRepository()(implicit system: ActorSystem, session: CassandraSession) {

  import net.nmoncho.helenus.pekko._
  import system.dispatcher

  private val pageFetchTimeout = 5.seconds
  private val queries          = new HotelRepository.Queries()

  // Q1. Find hotels near given poi
  def findByPOI(poiName: String): Source[Hotel, NotUsed] =
    queries.byPoi.asReadSource(poiName).map { case (id, name, phone, address) =>
      Hotel.byPoi(id, name, phone, address)
    }

  // Q2. Find information about a hotel
  def findById(id: String): Future[Option[Hotel]] =
    queries.byId.asReadSource(id).runWith(Sink.headOption)

  // Q3. Find pois near a hotel
  def findPOIsByHotel(id: String): Source[PointOfInterest, NotUsed] =
    queries.poiByHotel.asReadSource(id)

  // Q4. Find available rooms by hotel / date
  def availableRooms(id: String, date: LocalDate): Source[HotelRepository.RoomNumber, NotUsed] =
    queries.availableRoomsByHotel.asReadSource(id, date).collect {
      case (roomNumber, isAvailable) if isAvailable =>
        roomNumber
    }

  // Q5. Find amenities for a room
  def roomAmenities(id: String, roomNumber: HotelRepository.RoomNumber): Source[Amenity, NotUsed] =
    queries.roomAmenities.asReadSource(id, roomNumber)
}

object HotelRepository {

  type HotelId    = String
  type RoomNumber = Short

  class Queries()(implicit session: CassandraSession, ec: ExecutionContext) {

    import net.nmoncho.helenus._
    import net.nmoncho.helenus.pekko._

    final val byPoi =
      """SELECT hotel_id, name, phone, address
        |FROM hotels_by_poi
        |WHERE poi_name = ?""".stripMargin.toCQLAsync
        .prepare[String]
        .as[(String, String, String, Address)]

    final val byId =
      "SELECT * FROM hotels WHERE id = ?".toCQLAsync
        .prepare[String]
        .as[Hotel]

    final val poiByHotel =
      """SELECT poi_name as name, description
        |FROM pois_by_hotel
        |WHERE hotel_id = ?""".stripMargin.toCQLAsync
        .prepare[String]
        .as[PointOfInterest]

    final val availableRoomsByHotel =
      """SELECT room_number, is_available
        |FROM available_rooms_by_hotel_date
        |WHERE hotel_id = ? AND date = ?""".stripMargin.toCQLAsync
        .prepare[String, LocalDate]
        .as[(Short, Boolean)]

    final val roomAmenities =
      """SELECT amenity_name as name, description
        |FROM amenities_by_room
        |WHERE hotel_id = ? AND room_number = ?""".stripMargin.toCQLAsync
        .prepare[String, RoomNumber]
        .as[Amenity]

  }
}
