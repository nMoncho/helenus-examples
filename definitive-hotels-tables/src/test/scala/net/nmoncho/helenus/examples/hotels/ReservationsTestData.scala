package net.nmoncho.helenus.examples.hotels

import com.datastax.oss.driver.api.core.CqlSession
import net.nmoncho.helenus.examples.hotels.models.Guest
import net.nmoncho.helenus.examples.hotels.models.Address
import net.nmoncho.helenus.examples.hotels.models.Reservation
import net.nmoncho.helenus.examples.hotels.models.ReservationByGuest
import net.nmoncho.helenus.examples.hotels.repositories.Tables
import java.time.LocalDate
import java.util.UUID

object ReservationsTestData {

  def insertTestData()(implicit session: CqlSession): Unit = {
    import net.nmoncho.helenus._

    // The base table; the `reservations_by_confirmation` materialized view is
    // maintained by Cassandra, so it is never written to directly.
    Reservations.all.foreach(r => Tables.ReservationsByHotelDate.insertFrom(r).execute())

    for {
      reservation <- Reservations.all
      guest <- Guests.all.find(_.confirmNumber == reservation.confirmNumber)
    } Tables.ReservationsByGuest
      .insertFrom(
        ReservationByGuest(
          guest.lastName,
          reservation.hotelId,
          reservation.startDate,
          reservation.endDate,
          reservation.roomNumber,
          reservation.confirmNumber,
          reservation.guestId
        )
      )
      .execute()

    Guests.all.foreach(g => Tables.Guests.insertFrom(g).execute())
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
      sallySmithJKL012.guestId,
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
      johnDoeABC123.guestId,
      LocalDate.of(2023, 1, 1),
      LocalDate.of(2023, 1, 5)
    )

    val def456 = Reservation(
      "DEF456",
      "h1",
      201,
      janeDoeDEF456.guestId,
      LocalDate.of(2023, 1, 6),
      LocalDate.of(2023, 1, 10)
    )

    val ghi789 = Reservation(
      "GHI789",
      "h3",
      301,
      bobSmithGHI789.guestId,
      LocalDate.of(2023, 1, 11),
      LocalDate.of(2023, 1, 15)
    )

    val jkl012 = Reservation(
      "JKL012",
      "h4",
      401,
      sallySmithJKL012.guestId,
      LocalDate.of(2023, 1, 16),
      LocalDate.of(2023, 1, 20)
    )

    val rsp214 = Reservation(
      "RSP214",
      "h5",
      401,
      sallySmithJKL012.guestId,
      LocalDate.of(2023, 1, 16),
      LocalDate.of(2023, 1, 20)
    )

    val mno345 = Reservation(
      "MNO345",
      "h5",
      501,
      tomJonesMNO345.guestId,
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
