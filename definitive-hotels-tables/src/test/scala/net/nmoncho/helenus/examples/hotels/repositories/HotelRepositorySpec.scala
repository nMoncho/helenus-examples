package net.nmoncho.helenus.examples.hotels.repositories

import com.datastax.oss.driver.api.core.CqlSession
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import net.nmoncho.helenus.examples.hotels.HotelsTestData
import java.time.LocalDate

class HotelRepositorySpec extends AnyWordSpec with Matchers with CassandraSpec {
  import net.nmoncho.helenus._

  implicit lazy val cqlSession: CqlSession = session

  "HotelRepository" should {
    "query hotels by POI" in {
      val repository = new HotelRepository()

      repository.findByPOI(
        HotelsTestData.PointOfInterests.rotterdamEuromast.name
      ) should not be empty
    }

    "query hotel by id" in {
      val repository = new HotelRepository()

      repository.findById(HotelsTestData.Hotels.h1.id) shouldBe Some(HotelsTestData.Hotels.h1)
    }

    "query POI by hotel" in {
      val repository = new HotelRepository()

      repository
        .findPOIsByHotel(HotelsTestData.Hotels.h1.id)
        .map(_.name)
        .toSet shouldBe HotelsTestData.Hotels.h1.pois
    }

    "query available room" in {
      val repository = new HotelRepository()

      val january1st =
        repository.availableRooms(HotelsTestData.Hotels.h1.id, LocalDate.parse("2023-01-01"))
      january1st should not be empty

      withClue("only odd room numbers are available on oodd days") {
        january1st(11.toShort) shouldBe true
        january1st(12.toShort) shouldBe false
      }
    }

    "query room amenities" in {
      val repository = new HotelRepository()

      val amenities = repository.roomAmenities(HotelsTestData.Hotels.h1.id, 1)
      amenities should not be empty
    }
  }

  override def beforeAll(): Unit = {
    super.beforeAll()
    executeFile("hotels.cql")
    HotelsTestData.insertTestData()
  }
}
