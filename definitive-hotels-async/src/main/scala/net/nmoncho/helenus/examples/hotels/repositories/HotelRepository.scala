package net.nmoncho.helenus.examples.hotels.repositories

import net.nmoncho.helenus.examples.hotels.models.Hotel
import net.nmoncho.helenus.examples.hotels.models.Amenity
import net.nmoncho.helenus.examples.hotels.models.Address
import net.nmoncho.helenus.examples.hotels.models.PointOfInterest
import java.time.LocalDate
import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationInt
import com.datastax.oss.driver.api.core.CqlSession
import com.datastax.oss.driver.api.core.MappedAsyncPagingIterable

class HotelRepository()(implicit session: CqlSession) {
  import net.nmoncho.helenus._

  private val pageFetchTimeout = 5.seconds
  private val queries          = new HotelRepository.Queries()

  // Q1. Find hotels near given poi
  def findByPOI(poiName: String)(implicit ec: ExecutionContext): Future[Seq[Hotel]] =
    queries.byPoi
      .executeAsync(poiName)
      .map(
        // using `iter` works similarly to fetching all pages synchronously
        // it only block while fetching the following page
        _.iter(pageFetchTimeout).map { case (id, name, phone, address) =>
          Hotel.byPoi(id, name, phone, address)
        }.toList
      )

  // Q2. Find information about a hotel
  def findById(id: String)(implicit ec: ExecutionContext): Future[Option[Hotel]] =
    queries.byId
      .executeAsync(id)
      .map(
        _.currPage.nextOption()
      ) // we only care about the first result, from the first page (ie. currPage)

  // Q3. Find pois near a hotel
  def findPOIsByHotel(id: String)(implicit ec: ExecutionContext): Future[Seq[PointOfInterest]] =
    // Unlike Q1, this is fully asynchronous, but all still pages are fetched in one go
    queries.poiByHotel.executeAsync(id).flatMap { asyncPage =>
      fetchPage(asyncPage.currPage, asyncPage).map(_.toList)
    }

  // Q4. Find available rooms by hotel / date
  def availableRooms(id: String, date: LocalDate)(
      implicit ec: ExecutionContext
  ): Future[Set[HotelRepository.RoomNumber]] =
    queries.availableRoomsByHotel.executeAsync(id, date).flatMap { asyncPage =>
      fetchPage(asyncPage.currPage, asyncPage).map(_.collect {
        case (roomNumber, isAvailable) if isAvailable =>
          roomNumber
      }.toSet)
    }

  // Q5. Find amenities for a room
  def roomAmenities(id: String, roomNumber: HotelRepository.RoomNumber)(
      implicit ec: ExecutionContext
  ): Future[MappedAsyncPagingIterable[Amenity]] =
    // Unlike Q1 and Q3, this gives page fetching control to the user
    queries.roomAmenities.executeAsync(id, roomNumber)
}

object HotelRepository {

  type HotelId    = String
  type RoomNumber = Short

  class Queries()(implicit session: CqlSession) {
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
