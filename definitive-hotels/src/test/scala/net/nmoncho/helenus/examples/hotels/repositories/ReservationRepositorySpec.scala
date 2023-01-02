package net.nmoncho.helenus.examples.hotels.repositories

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import net.nmoncho.helenus.examples.hotels.ReservationsTestData._

class ReservationRepositorySpec extends AnyWordSpec with Matchers with CassandraSpec {
  import net.nmoncho.helenus._

  implicit lazy val cqlSession: CqlSessionExtension = session.toScala

  "ReservationRepository" should {
    "find a reservation by confirmation number" in {
      val repository = new ReservationRepository()

      repository.findReservationByConfirmation(
        Reservations.abc123.confirmationNumber
      ) shouldBe Some(Reservations.abc123)
      repository.findReservationByConfirmation("FOO901") shouldBe None
    }

    "find a reservation by hotel id and date" in {
      val repository = new ReservationRepository()

      repository.findReservationByHotelAndDate(
        Reservations.abc123.hotelId,
        Reservations.abc123.startDate
      ) shouldBe Set(Reservations.abc123)
      repository.findReservationByHotelAndDate(
        Reservations.mno345.hotelId,
        Reservations.mno345.startDate
      ) shouldBe Set(Reservations.mno345, Reservations.rsp214)
    }

    "find a reservation by guest name" in {
      val repository = new ReservationRepository()

      val reservations = repository.findReservationByGuestName(Guests.sallySmithJKL012.lastName)
      reservations should have size 3 // one for Bob Smith, two for Sally Smith
    }

    "find a guest by id" in {
      val repository = new ReservationRepository()

      val sallyOpt = repository.findGuestById(Guests.sallySmithJKL012.id)
      sallyOpt shouldBe defined
      sallyOpt.foreach { sally =>
        sally.firstName shouldBe Guests.sallySmithJKL012.firstName
        sally.title shouldBe Guests.sallySmithJKL012.title
        sally.emails shouldBe Guests.sallySmithJKL012.emails
      }
    }
  }

  override def beforeAll(): Unit = {
    super.beforeAll()
    executeFile("reservations.cql")
    insertTestData()
  }
}
