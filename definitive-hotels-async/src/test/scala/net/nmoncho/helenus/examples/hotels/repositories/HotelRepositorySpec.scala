package net.nmoncho.helenus.examples.hotels.repositories

import com.datastax.oss.driver.api.core.CqlSession
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import net.nmoncho.helenus.examples.hotels.HotelsTestData
import java.time.LocalDate
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.Span
import org.scalatest.time.Seconds

class HotelRepositorySpec extends AnyWordSpec with Matchers with CassandraSpec with ScalaFutures {
  import net.nmoncho.helenus._

  import scala.concurrent.ExecutionContext.Implicits.global

  implicit lazy val cqlSession: CqlSession = session

  "HotelRepository" should {
    "query hotels by POI" in {
      val repository = new HotelRepository()

      whenReady(
        repository.findByPOI(
          HotelsTestData.PointOfInterests.rotterdamEuromast.name
        )
      )(hotels => hotels should not be empty)
    }

    "query hotel by id" in {
      val repository = new HotelRepository()

      whenReady(repository.findById(HotelsTestData.Hotels.h1.id)) { hotel =>
        hotel shouldBe Some(HotelsTestData.Hotels.h1)
      }
    }

    "query POI by hotel" in {
      val repository = new HotelRepository()

      whenReady(
        repository
          .findPOIsByHotel(HotelsTestData.Hotels.h1.id)
          .map(fut => fut.map(poi => poi.name))
      )(_.toSet shouldBe HotelsTestData.Hotels.h1.pois)
    }

    "query available room" in {
      val repository = new HotelRepository()

      whenReady(
        repository.availableRooms(HotelsTestData.Hotels.h1.id, LocalDate.parse("2023-01-01"))
      ) { january1st =>
        january1st should not be empty

        withClue("only odd room numbers are available on oodd days") {
          january1st(11.toShort) shouldBe true
          january1st(12.toShort) shouldBe false
        }
      }
    }

    "query room amenities" in {
      val repository = new HotelRepository()

      val result = whenReady(repository.roomAmenities(HotelsTestData.Hotels.h1.id, 1)) { result =>
        result.currPage should not be empty

        result
      }

      // With this API we have to fetch pages ourselves
      whenReady(result.nextPage) { nextPage =>
        nextPage shouldBe empty
      }
    }
  }

  override def beforeAll(): Unit = {
    super.beforeAll()
    executeFile("hotels.cql")
    HotelsTestData.insertTestData()
  }

  override implicit def patienceConfig: PatienceConfig = PatienceConfig(Span(6, Seconds))
}
