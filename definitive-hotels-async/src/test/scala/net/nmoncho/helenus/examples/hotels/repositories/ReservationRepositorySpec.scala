package net.nmoncho.helenus.examples.hotels.repositories

import com.datastax.oss.driver.api.core.CqlSession
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import net.nmoncho.helenus.examples.hotels.ReservationsTestData._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.Span
import org.scalatest.time.Seconds

class ReservationRepositorySpec
    extends AnyWordSpec
    with Matchers
    with CassandraSpec
    with ScalaFutures {
  import net.nmoncho.helenus._

  import scala.concurrent.ExecutionContext.Implicits.global

  implicit lazy val cqlSession: CqlSession = session

  "ReservationRepository" should {
    "find a reservation by confirmation number" in {
      val repository = new ReservationRepository()

      whenReady(repository.findReservationByConfirmation(Reservations.abc123.confirmationNumber))(
        reservation => reservation shouldBe Some(Reservations.abc123)
      )

      whenReady(repository.findReservationByConfirmation("FOO901"))(reservation =>
        reservation shouldBe None
      )
    }

    "find a reservation by hotel id and date" in {
      val repository = new ReservationRepository()

      whenReady(
        repository.findReservationByHotelAndDate(
          Reservations.abc123.hotelId,
          Reservations.abc123.startDate
        )
      )(reservations => reservations shouldBe Set(Reservations.abc123))

      whenReady(
        repository.findReservationByHotelAndDate(
          Reservations.mno345.hotelId,
          Reservations.mno345.startDate
        )
      )(reservations => reservations shouldBe Set(Reservations.mno345, Reservations.rsp214))
    }

    "find a reservation by guest name" in {
      val repository = new ReservationRepository()

      whenReady(repository.findReservationByGuestName(Guests.sallySmithJKL012.lastName)) {
        reservations =>
          reservations should have size 3 // one for Bob Smith, two for Sally Smith
      }
    }

    "find a guest by id" in {
      val repository = new ReservationRepository()

      whenReady(repository.findGuestById(Guests.sallySmithJKL012.id)) { sallyOpt =>
        sallyOpt shouldBe defined
        sallyOpt.foreach { sally =>
          sally.firstName shouldBe Guests.sallySmithJKL012.firstName
          sally.title shouldBe Guests.sallySmithJKL012.title
          sally.emails shouldBe Guests.sallySmithJKL012.emails
        }
      }
    }
  }

  override def beforeAll(): Unit = {
    super.beforeAll()
    executeFile("reservations.cql")
    insertTestData()
  }

  override implicit def patienceConfig: PatienceConfig = PatienceConfig(Span(6, Seconds))
}
