package net.nmoncho.helenus.examples.hotels.repositories

import net.nmoncho.helenus.examples.hotels.LocalSpark
import net.nmoncho.helenus.examples.hotels.ReservationsTestData._
import org.apache.spark.SparkContext
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class ReservationRepositorySpec extends AnyWordSpec with Matchers with CassandraSpec {

  private def sc: SparkContext = LocalSpark.session.sparkContext

  "ReservationRepository (Spark)" should {
    "write reservations with the CQL-first sink and read them back" in {
      val reservations = Reservations.all

      ReservationRepository.saveReservations(sc.parallelize(reservations))

      ReservationRepository
        .allReservations(sc)
        .collect()
        .toList should contain theSameElementsAs reservations
    }

    "write guests (map-of-UDT column included) and read them back" in {
      ReservationRepository.saveGuests(sc.parallelize(Guests.all.toSeq))

      val read = ReservationRepository.allGuests(sc).collect().toList

      // sallySmithJKL012 and sallySmithRSP214 share a guest_id, so the table keeps one row
      // per distinct id.
      read.map(_.id).toSet shouldBe Guests.all.map(_.id)
      read should contain(Guests.johnDoeABC123)
    }
  }

  override def beforeAll(): Unit = {
    super.beforeAll()
    executeFile("reservations.cql")
  }
}
