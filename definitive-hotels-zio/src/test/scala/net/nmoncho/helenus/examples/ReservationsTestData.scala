package net.nmoncho.helenus.examples

import net.nmoncho.helenus.examples.models.{ Address, Guest, Reservation }

import java.time.LocalDate
import java.util.UUID

object ReservationsTestData {
  import net.nmoncho.helenus.zio._
  import zio._

  def insertTestData(): ZIO[ZCqlSession, CassandraException, _] = {
    val insertReservationByHotelDate =
      """INSERT INTO reservations_by_hotel_date(hotel_id, start_date, end_date, room_number, confirm_number, guest_id) VALUES(?, ?, ?, ?, ?, ?)""".stripMargin.toZCQL
        .prepare[String, LocalDate, LocalDate, Short, String, UUID]
        .mapError { ex =>
          ex.printStackTrace()
          ex
        }

    val reservationDML = ZIO.foreach(Reservations.all) { r =>
      insertReservationByHotelDate.execute(
        r.hotelId,
        r.startDate,
        r.endDate,
        r.roomNumber,
        r.confirmationNumber,
        r.guestId
      )
    }

    val insertReservationByGuest =
      """INSERT INTO reservations_by_guest(guest_last_name, hotel_id, start_date, end_date, room_number, confirm_number, guest_id)
        |VALUES(?, ?, ?, ?, ?, ?, ?)""".stripMargin.toZCQL
        .prepare[String, String, LocalDate, LocalDate, Short, String, UUID]

    val reservationGuestValues = for {
      reservation <- Reservations.all
      guest <- Guests.all.find(_.confirmationNumber == reservation.confirmationNumber)
    } yield (reservation, guest)

    val reservationGuestDML = ZIO.foreach(reservationGuestValues) { case (reservation, guest) =>
      insertReservationByGuest.execute(
        guest.lastName,
        reservation.hotelId,
        reservation.startDate,
        reservation.endDate,
        reservation.roomNumber,
        reservation.confirmationNumber,
        reservation.guestId
      )
    }

    val insertGuest =
      """INSERT INTO guests (guest_id, first_name, last_name, title, emails, phone_numbers, addresses, confirm_number)
      |VALUES (?, ?, ?, ?, ?, ?, ?, ?)""".stripMargin.toZCQL
        .prepare[UUID, String, String, Option[String], Set[String], List[String], Map[
          String,
          Address
        ], String]

    val guestDML = ZIO.foreach(Guests.all)(g =>
      insertGuest.execute(
        g.id,
        g.firstName,
        g.lastName,
        g.title,
        g.emails,
        g.phoneNumbers,
        g.addresses,
        g.confirmationNumber
      )
    )

    reservationDML *> reservationGuestDML *> guestDML
  }

  object Guests {
    val johnDoeABC123 = Guest(
      UUID.randomUUID(),
      "John",
      "Doe",
      Some("Mr."),
      Set("johndoe@example.com"),
      List("555-555-1212"),
      Map(
        "home" -> Address(
          "123 Main St.",
          "Anytown",
          "Anystate",
          "12345",
          "USA"
        )
      ),
      "ABC123"
    )

    val janeDoeDEF456 = Guest(
      UUID.randomUUID(),
      "Jane",
      "Doe",
      Some("Mrs."),
      Set("janedoe@example.com"),
      List("555-555-1212", "555-555-1213"),
      Map(
        "home" -> Address(
          "456 Main St.",
          "Anytown",
          "Anystate",
          "12345",
          "USA"
        ),
        "work" -> Address(
          "789 Main St.",
          "Anytown",
          "Anystate",
          "12345",
          "USA"
        )
      ),
      "DEF456"
    )

    val bobSmithGHI789 = Guest(
      UUID.randomUUID(),
      "Bob",
      "Smith",
      None,
      Set("bobsmith@example.com"),
      List("555-555-1212"),
      Map(
        "home" -> Address(
          "321 Main St.",
          "Anytown",
          "Anystate",
          "12345",
          "USA"
        )
      ),
      "GHI789"
    )

    val sallySmithJKL012 = Guest(
      UUID.randomUUID(),
      "Sally",
      "Smith",
      Some("Ms."),
      Set("sallysmith@example.com"),
      List("555-555-1212", "555-555-1213"),
      Map(
        "home" -> Address(
          "654 Main St.",
          "Anytown",
          "Anystate",
          "12345",
          "USA"
        )
      ),
      "JKL012"
    )

    val sallySmithRSP214 = Guest(
      sallySmithJKL012.id,
      "Sally",
      "Smith",
      Some("Ms."),
      Set("sallysmith@example.com"),
      List("555-555-1212", "555-555-1213"),
      Map(
        "home" -> Address(
          "654 Main St.",
          "Anytown",
          "Anystate",
          "12345",
          "USA"
        )
      ),
      "RSP214"
    )

    val tomJonesMNO345 = Guest(
      UUID.randomUUID(),
      "Tom",
      "Jones",
      Some("Dr."),
      Set("tomjones@example.com"),
      List("555-555-1212", "555-555-1213", "555-555-1214"),
      Map(
        "home" -> Address(
          "987 Main St.",
          "Anytown",
          "Anystate",
          "12345",
          "USA"
        )
      ),
      "MNO345"
    )

    val all: Set[Guest] = Set(
      johnDoeABC123,
      janeDoeDEF456,
      bobSmithGHI789,
      sallySmithJKL012,
      sallySmithRSP214,
      tomJonesMNO345
    )
  }

  object Reservations {
    import Guests._

    val abc123 = Reservation(
      "ABC123",
      "h1",
      101,
      johnDoeABC123.id,
      LocalDate.of(2023, 1, 1),
      LocalDate.of(2023, 1, 5)
    )

    val def456 = Reservation(
      "DEF456",
      "h1",
      201,
      janeDoeDEF456.id,
      LocalDate.of(2023, 1, 6),
      LocalDate.of(2023, 1, 10)
    )

    val ghi789 = Reservation(
      "GHI789",
      "h3",
      301,
      bobSmithGHI789.id,
      LocalDate.of(2023, 1, 11),
      LocalDate.of(2023, 1, 15)
    )

    val jkl012 = Reservation(
      "JKL012",
      "h4",
      401,
      sallySmithJKL012.id,
      LocalDate.of(2023, 1, 16),
      LocalDate.of(2023, 1, 20)
    )

    val rsp214 = Reservation(
      "RSP214",
      "h5",
      401,
      sallySmithJKL012.id,
      LocalDate.of(2023, 1, 16),
      LocalDate.of(2023, 1, 20)
    )

    val mno345 = Reservation(
      "MNO345",
      "h5",
      501,
      tomJonesMNO345.id,
      LocalDate.of(2023, 1, 16),
      LocalDate.of(2023, 1, 20)
    )

    val all: Seq[Reservation] = Seq(
      abc123,
      def456,
      ghi789,
      jkl012,
      rsp214,
      mno345
    )
  }
}
