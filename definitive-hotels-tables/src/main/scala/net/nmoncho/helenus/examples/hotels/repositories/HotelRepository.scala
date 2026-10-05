package net.nmoncho.helenus.examples.hotels.repositories

import com.datastax.oss.driver.api.core.CqlSession
import net.nmoncho.helenus.examples.hotels.models.Amenity
import net.nmoncho.helenus.examples.hotels.models.Hotel
import net.nmoncho.helenus.examples.hotels.models.PointOfInterest
import java.time.LocalDate

/** Repository backed entirely by the `helenus-tables` DSL: every query is built
  * from a [[Tables]] definition with `select(...).where(... === ?)` and prepared
  * once, then run with `execute`. The prepared statements are ordinary Helenus
  * `ScalaPreparedStatement`s, so paging (`to`, `nextOption`) works as usual.
  */
class HotelRepository()(implicit session: CqlSession) {
  import net.nmoncho.helenus._

  private val queries = new HotelRepository.Queries()

  // Q1. Find hotels near given poi
  def findByPOI(poiName: String): Seq[Hotel] =
    queries.byPoi
      .execute(poiName)
      .to(List)
      .map(h => Hotel.byPoi(h.hotelId, h.name, h.phone, h.address))

  // Q2. Find information about a hotel
  def findById(id: String): Option[Hotel] =
    queries.byId.execute(id).nextOption

  // Q3. Find pois near a hotel
  def findPOIsByHotel(id: String): Seq[PointOfInterest] =
    queries.poiByHotel
      .execute(id)
      .to(List)
      .map(p => PointOfInterest(p.poiName, p.description))

  // Q4. Find available rooms by hotel / date
  def availableRooms(id: String, date: LocalDate): Set[HotelRepository.RoomNumber] =
    queries.availableRoomsByHotel.execute(id, date).to(Set).collect {
      case room if room.isAvailable => room.roomNumber
    }

  // Q5. Find amenities for a room
  def roomAmenities(id: String, roomNumber: HotelRepository.RoomNumber): Set[Amenity] =
    queries.roomAmenities
      .execute(id, roomNumber)
      .to(Set)
      .map(a => Amenity(a.amenityName, a.description))
}

object HotelRepository {

  type HotelId    = String
  type RoomNumber = Short

  class Queries()(implicit session: CqlSession) {
    import net.nmoncho.helenus._
    import net.nmoncho.helenus.api.tables._
    import Tables._

    final val byPoi =
      HotelsByPoi.select().where(HotelsByPoi.poiName === ?).prepare

    final val byId =
      Hotels.select().where(Hotels.id === ?).prepare

    final val poiByHotel =
      PoisByHotel.select().where(PoisByHotel.hotelId === ?).prepare

    final val availableRoomsByHotel =
      AvailableRoomsByHotelDate
        .select()
        .where(AvailableRoomsByHotelDate.hotelId === ? and AvailableRoomsByHotelDate.date === ?)
        .prepare

    final val roomAmenities =
      AmenitiesByRoom
        .select()
        .where(AmenitiesByRoom.hotelId === ? and AmenitiesByRoom.roomNumber === ?)
        .prepare
  }
}
