package net.nmoncho.helenus.examples

import com.datastax.oss.driver.api.core.PagingIterable
import com.datastax.oss.driver.api.core.cql.Row
import net.nmoncho.helenus.examples.models.{ Address, Amenity, Hotel, PointOfInterest }

import java.time.LocalDate
import scala.collection.immutable
import scala.util.Try

object HotelsTestData {
  import net.nmoncho.helenus.zio._
  import zio._

  private val rnd = new scala.util.Random(0)

  def insertTestData(): ZIO[ZCqlSession, CassandraException, _] = {
    val insertHotelByPOI =
      """INSERT INTO hotels_by_poi(poi_name, hotel_id, name, phone, address)
        |VALUES (?, ?, ?, ?, ?)""".stripMargin.toZCQL
        .prepare[String, String, String, String, Address]

    val insertPoiByHotel =
      """INSERT INTO pois_by_hotel(poi_name, hotel_id, description)
        |VALUES (?, ?, ?)""".stripMargin.toZCQL
        .prepare[String, String, String]

    val hotelPoiValues = for {
      hotel <- Hotels.all
      poiName <- hotel.pois
      poi <- PointOfInterests.all.find(_.name == poiName)
    } yield (hotel, poiName, poi)

    val hotelPoiDML = ZIO.foreach(hotelPoiValues) { case (hotel, poiName, poi) =>
      insertHotelByPOI.execute(poiName, hotel.id, hotel.name, hotel.phone, hotel.address) *>
      insertPoiByHotel.execute(poiName, hotel.id, poi.description)
    }

    val insertHotel =
      """INSERT INTO hotels(id, name, phone, address, pois)
        |VALUES (?, ?, ?, ?, ?)""".stripMargin.toZCQL
        .prepare[String, String, String, Address, Set[String]]

    val hotelsDML =
      ZIO.foreach(Hotels.all)(h => insertHotel.execute(h.id, h.name, h.phone, h.address, h.pois))

    val insertAvailableRooms =
      """INSERT INTO available_rooms_by_hotel_date(hotel_id, date, room_number, is_available)
        |VALUES (?, ?, ?, ?)""".stripMargin.toZCQL
        .prepare[String, LocalDate, Short, Boolean]

    val roomValues = for {
      (hotel, rooms) <- Hotels.availableRooms
      room <- (0 until rooms)
      january1st = LocalDate.parse("2023-01-01")
      date <- (0 to 31).map(days => january1st.plusDays(days))
    } yield (
      hotel.id,
      date,
      room.toShort,
      // even room are available on even days of the month
      date.getDayOfMonth() % 2 == room % 2
    )

    val insertAvailableRoomsDML = ZIO.foreach(roomValues) {
      case (hotelId, date, roomNumber, isAvailable) =>
        insertAvailableRooms.execute(hotelId, date, roomNumber, isAvailable)
    }

    val insertAmenities =
      """INSERT INTO amenities_by_room(hotel_id, room_number, amenity_name, description)
        |VALUES (?, ?, ?, ?)""".stripMargin.toZCQL
        .prepare[String, Short, String, String]

    val amentiesSize = Amenities.all.size
    val amenities    = Amenities.all.toVector

    val amenityValues = for {
      (hotel, rooms) <- Hotels.availableRooms
      room <- (0 until rooms)
      amenityAmount = rnd.nextInt(amentiesSize) + 1
      amenity <- (0 until amenityAmount).map(amenities.apply)
    } yield (hotel.id, room.toShort, amenity.name, amenity.description)

    val amenitiesDML = ZIO.foreach(amenityValues) {
      case (hotelId, roomNumber, amenity, description) =>
        insertAmenities.execute(hotelId, roomNumber, amenity, description)
    }

    hotelPoiDML *> hotelsDML *> insertAvailableRoomsDML *> amenitiesDML
  }

  object PointOfInterests {

    val rotterdamErasmusBridge = PointOfInterest(
      name        = "Erasmus Bridge",
      description = "Iconic cable-stayed bridge in Rotterdam."
    )

    val rotterdamZoo = PointOfInterest(
      name        = "Rotterdam Zoo",
      description = "Zoo located in the city center of Rotterdam."
    )

    val rotterdamMarkthal = PointOfInterest(
      name        = "Markthal Rotterdam",
      description = "Market hall in Rotterdam with a distinctive arched roof."
    )

    val rotterdamEuromast = PointOfInterest(
      name        = "Euromast",
      description = "Tall observation tower in Rotterdam with panoramic views of the city."
    )

    val rotterdamBoijmans = PointOfInterest(
      name = "Museum Boijmans Van Beuningen",
      description =
        "Museum in Rotterdam featuring art and design from the Middle Ages to the present day."
    )

    val all: Set[PointOfInterest] = Set(
      rotterdamErasmusBridge,
      rotterdamZoo,
      rotterdamMarkthal,
      rotterdamEuromast,
      rotterdamBoijmans
    )
  }

  object Hotels {
    import PointOfInterests._

    val h1 = Hotel(
      "h1",
      "The James Rotterdam",
      "+31 10 710 9000",
      Address(
        "Wijnhaven 107",
        "Rotterdam",
        "South Holland",
        "3011 WN",
        "Netherlands"
      ),
      Set(rotterdamErasmusBridge.name, rotterdamZoo.name)
    )

    val h2 = Hotel(
      "h2",
      "Rotterdam Marriott Hotel",
      "+31 10 710 1515",
      Address(
        "Weena 686",
        "Rotterdam",
        "South Holland",
        "3012 CN",
        "Netherlands"
      ),
      Set(rotterdamErasmusBridge.name, rotterdamZoo.name, rotterdamMarkthal.name)
    )

    val h3 = Hotel(
      "h3",
      "Mainport Hotel Rotterdam",
      "+31 10 217 6666",
      Address(
        "Leuvehaven 77",
        "Rotterdam",
        "South Holland",
        "3011 EA",
        "Netherlands"
      ),
      Set(rotterdamErasmusBridge.name, rotterdamZoo.name, rotterdamMarkthal.name)
    )

    val h4 = Hotel(
      "h4",
      "The New York Hotel Rotterdam",
      "+31 10 217 3000",
      Address(
        "Meent 78-82",
        "Rotterdam",
        "South Holland",
        "3011 JM",
        "Netherlands"
      ),
      Set(rotterdamErasmusBridge.name, rotterdamZoo.name, rotterdamMarkthal.name)
    )

    val h5 = Hotel(
      "h5",
      "CitizenM Rotterdam",
      "+31 10 717 9999",
      Address(
        "Wilhelminakade 137",
        "Rotterdam",
        "South Holland",
        "3072 AP",
        "Netherlands"
      ),
      Set(rotterdamErasmusBridge.name, rotterdamZoo.name, rotterdamEuromast.name)
    )

    val h6 = Hotel(
      "h6",
      "Hampshire Hotel - Savoy Rotterdam",
      "+31 10 413 4333",
      Address(
        "Westzeedijk 345",
        "Rotterdam",
        "South Holland",
        "3024 AP",
        "Netherlands"
      ),
      Set(rotterdamErasmusBridge.name, rotterdamZoo.name, rotterdamEuromast.name)
    )

    val h7 = Hotel(
      "h7",
      "Euromast & Markthal Hotel",
      "+31 10 436 3636",
      Address(
        "Parkhaven 20",
        "Rotterdam",
        "South Holland",
        "3016 GM",
        "Netherlands"
      ),
      Set(rotterdamEuromast.name, rotterdamMarkthal.name)
    )

    val h8 = Hotel(
      "h8",
      "Museum Boijmans Van Beuningen & Rotterdam Zoo Hotel",
      "+31 10 441 9400",
      Address(
        "Museumpark 18-20",
        "Rotterdam",
        "South Holland",
        "3015 CX",
        "Netherlands"
      ),
      Set(rotterdamBoijmans.name, rotterdamZoo.name)
    )

    val h9 = Hotel(
      "h9",
      "Erasmus Bridge, Rotterdam Zoo, & Euromast Hotel",
      "+31 10 217 0000",
      Address(
        "Wilhelminakade 7",
        "Rotterdam",
        "South Holland",
        "3072 AP",
        "Netherlands"
      ),
      Set(rotterdamErasmusBridge.name, rotterdamZoo.name, rotterdamEuromast.name)
    )

    val h10 = Hotel(
      "h10",
      "Markthal, Museum Boijmans Van Beuningen, & Rotterdam Zoo Hotel",
      "+31 10 298 5800",
      Address(
        "Ds. Jan Scharpstraat 298",
        "Rotterdam",
        "South Holland",
        "3067 GJ",
        "Netherlands"
      ),
      Set(rotterdamMarkthal.name, rotterdamBoijmans.name, rotterdamZoo.name)
    )

    val h11 = Hotel(
      "h11",
      "Erasmus Bridge & Markthal Hotel",
      "+31 10 217 0000",
      Address(
        "Wilhelminakade 7",
        "Rotterdam",
        "South Holland",
        "3072 AP",
        "Netherlands"
      ),
      Set(rotterdamErasmusBridge.name, rotterdamMarkthal.name)
    )

    val h12 = Hotel(
      "h12",
      "Euromast & Museum Boijmans Van Beuningen Hotel",
      "+31 10 436 3636",
      Address(
        "Parkhaven 20",
        "Rotterdam",
        "South Holland",
        "3016 GM",
        "Netherlands"
      ),
      Set(rotterdamEuromast.name, rotterdamBoijmans.name)
    )

    val h13 = Hotel(
      "h13",
      "Rotterdam Zoo & Markthal Hotel",
      "+31 10 445 5000",
      Address(
        "Blijdorpweg 60",
        "Rotterdam",
        "South Holland",
        "3055 JG",
        "Netherlands"
      ),
      Set(rotterdamZoo.name, rotterdamMarkthal.name)
    )

    val h14 = Hotel(
      "h14",
      "Erasmus Bridge, Euromast, & Rotterdam Zoo Hotel",
      "+31 10 217 0000",
      Address(
        "Wilhelminakade 7",
        "Rotterdam",
        "South Holland",
        "3072 AP",
        "Netherlands"
      ),
      Set(rotterdamErasmusBridge.name, rotterdamEuromast.name, rotterdamZoo.name)
    )

    val h15 = Hotel(
      "h15",
      "Markthal, Museum Boijmans Van Beuningen, & Euromast Hotel",
      "+31 10 298 5800",
      Address(
        "Ds. Jan Scharpstraat 298",
        "Rotterdam",
        "South Holland",
        "3067 GJ",
        "Netherlands"
      ),
      Set(rotterdamMarkthal.name, rotterdamBoijmans.name, rotterdamEuromast.name)
    )

    val h16 = Hotel(
      "h16",
      "Erasmus Bridge, Museum Boijmans Van Beuningen, & Markthal Hotel",
      "+31 10 217 0000",
      Address(
        "Wilhelminakade 7",
        "Rotterdam",
        "South Holland",
        "3072 AP",
        "Netherlands"
      ),
      Set(rotterdamErasmusBridge.name, rotterdamBoijmans.name, rotterdamMarkthal.name)
    )

    val h17 = Hotel(
      "h17",
      "Rotterdam Zoo, Euromast, & Museum Boijmans Van Beuningen Hotel",
      "+31 10 445 5000",
      Address(
        "Blijdorpweg 60",
        "Rotterdam",
        "South Holland",
        "3055 JG",
        "Netherlands"
      ),
      Set(rotterdamZoo.name, rotterdamEuromast.name, rotterdamBoijmans.name)
    )

    val h18 = Hotel(
      "h18",
      "Erasmus Bridge, Rotterdam Zoo, Euromast, & Museum Boijmans Van Beuningen Hotel",
      "+31 10 217 0000",
      Address(
        "Wilhelminakade 7",
        "Rotterdam",
        "South Holland",
        "3072 AP",
        "Netherlands"
      ),
      Set(
        rotterdamErasmusBridge.name,
        rotterdamZoo.name,
        rotterdamEuromast.name,
        rotterdamBoijmans.name
      )
    )

    val h19 = Hotel(
      "h19",
      "Rotterdam Zoo, Euromast, Museum Boijmans Van Beuningen, & Markthal Hotel",
      "+31 10 445 5000",
      Address(
        "Blijdorpweg 60",
        "Rotterdam",
        "South Holland",
        "3055 JG",
        "Netherlands"
      ),
      Set(rotterdamZoo.name, rotterdamEuromast.name, rotterdamBoijmans.name, rotterdamMarkthal.name)
    )

    val h20 = Hotel(
      "h20",
      "Erasmus Bridge, Rotterdam Zoo, Euromast, Museum Boijmans Van Beuningen, & Markthal Hotel",
      "+31 10 217 0000",
      Address(
        "Wilhelminakade 7",
        "Rotterdam",
        "South Holland",
        "3072 AP",
        "Netherlands"
      ),
      Set(
        rotterdamErasmusBridge.name,
        rotterdamZoo.name,
        rotterdamEuromast.name,
        rotterdamBoijmans.name,
        rotterdamMarkthal.name
      )
    )

    val all: Seq[Hotel] = Seq(
      h1,
      h2,
      h3,
      h4,
      h5,
      h6,
      h7,
      h8,
      h9,
      h10
    )

    val availableRooms: Map[Hotel, Short] = Map(
      h1 -> 25,
      h2 -> 15,
      h3 -> 20,
      h4 -> 12,
      h5 -> 17,
      h6 -> 22,
      h7 -> 10,
      h8 -> 17,
      h9 -> 20,
      h10 -> 15,
      h11 -> 25,
      h12 -> 20,
      h13 -> 17,
      h14 -> 25,
      h15 -> 17,
      h16 -> 25,
      h17 -> 20,
      h18 -> 25,
      h19 -> 20,
      h20 -> 25
    )
  }

  object Amenities {
    val freeBreakfast = Amenity("Free breakfast", "Complimentary breakfast served daily.")
    val fitnessCenter = Amenity(
      "Fitness center",
      "On-site fitness center with treadmills, weight machines, and free weights."
    )
    val swimmingPool = Amenity("Swimming pool", "Outdoor swimming pool open seasonally.")
    val spa = Amenity("Spa", "On-site spa offering a variety of massage and beauty treatments.")
    val businessCenter =
      Amenity("Business center", "Business center with computers, printers, and a fax machine.")
    val freeWifi = Amenity(
      "Free Wi-Fi",
      "Complimentary high-speed Wi-Fi access in all guest rooms and public areas."
    )
    val airCo       = Amenity("Air conditioning", "Air conditioning in all guest rooms.")
    val laundry     = Amenity("Laundry facilities", "On-site guest laundry facilities.")
    val freeParking = Amenity("Free parking", "Complimentary self-parking in the hotel's lot.")
    val roomService = Amenity("Room service", "Room service available 24 hours a day.")

    val all: Seq[Amenity] = Seq(
      freeBreakfast,
      fitnessCenter,
      swimmingPool,
      spa,
      businessCenter,
      freeWifi,
      airCo,
      laundry,
      freeParking,
      roomService
    )
  }
}
