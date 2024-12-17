package net.nmoncho.helenus.examples.repositories

import net.nmoncho.helenus.examples.ReservationsTestData.{ Guests, Reservations }
import net.nmoncho.helenus.examples.{ HotelsTestData, ReservationsTestData, ZCassandraSpec }
import zio.test._
import zio.test.Assertion._

import java.time.LocalDate

object RepositoriesSpec extends ZCassandraSpec {

  override def spec = suite("Helenus")(
    test("query hotel by id") {
      assertZIO(HotelRepository.findById(HotelsTestData.Hotels.h1.id))(isSome)
    },
    test("query POI by hotel") {
      assertZIO(HotelRepository.findPOIsByHotel(HotelsTestData.Hotels.h1.id))(isNonEmpty)
    },
    test("query available room") {
      assertZIO(
        HotelRepository.availableRooms(HotelsTestData.Hotels.h1.id, LocalDate.parse("2023-01-01"))
      )(isNonEmpty)
    },
    test("query room amenities") {
      assertZIO(HotelRepository.roomAmenities(HotelsTestData.Hotels.h1.id, 1))(isNonEmpty)
    },
    test("find a reservation by confirmation number") {
      assertZIO(
        ReservationRepository.findReservationByConfirmation(Reservations.abc123.confirmationNumber)
      )(isSome)
    },
    test("find a reservation by guest name") {
      assertZIO(ReservationRepository.findReservationByGuestName(Guests.sallySmithJKL012.lastName))(
        isNonEmpty
      )
    },
    test("find a guest by id") {
      assertZIO(ReservationRepository.findGuestById(Guests.sallySmithJKL012.id))(isSome)
    }
  ) @@ TestAspect.beforeAll(beforeAll()) @@ TestAspect.sequential

  private def beforeAll() = for {
    _ <- createKeyspace()
    _ <- executeFileDDL("hotels.cql")
    _ <- executeFileDDL("reservations.cql")
    _ <- HotelsTestData.insertTestData()
    _ <- ReservationsTestData.insertTestData()
  } yield ()
}
