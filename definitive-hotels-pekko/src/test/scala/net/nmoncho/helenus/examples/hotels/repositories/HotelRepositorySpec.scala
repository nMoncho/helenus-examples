package net.nmoncho.helenus.examples.hotels.repositories

import net.nmoncho.helenus.examples.hotels.HotelsTestData
import net.nmoncho.helenus.examples.hotels.models.{ Amenity, Hotel }
import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.stream.scaladsl.Sink
import org.apache.pekko.testkit.TestKit
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.time.{ Seconds, Span }
import org.scalatest.wordspec.AnyWordSpecLike

import java.time.LocalDate
import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

class HotelRepositorySpec
    extends TestKit(ActorSystem())
    with AnyWordSpecLike
    with Matchers
    with CassandraSpec
    with ScalaFutures {

  import system.dispatcher

  "HotelRepository" should {
    "query hotels by POI" in {
      val repository = new HotelRepository()

      whenReady(
        repository
          .findByPOI(
            HotelsTestData.PointOfInterests.rotterdamEuromast.name
          )
          .runWith(Sink.seq[Hotel])
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
          .map(_.name)
          .runWith(Sink.seq[String])
      )(_.toSet shouldBe HotelsTestData.Hotels.h1.pois)
    }

    "query available room" in {
      val repository = new HotelRepository()

      whenReady(
        repository
          .availableRooms(HotelsTestData.Hotels.h1.id, LocalDate.parse("2023-01-01"))
          .runWith(Sink.seq[HotelRepository.RoomNumber])
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

      whenReady(
        repository
          .roomAmenities(HotelsTestData.Hotels.h1.id, 1.toShort)
          .runWith(Sink.seq[Amenity])
      ) { result =>
        result should not be empty
      }
    }
  }

  override def beforeAll(): Unit = {
    super.beforeAll()
    executeFile("hotels.cql")

    Await.ready(
      HotelsTestData.insertTestData(),
      60.seconds
    )
  }

  override implicit def patienceConfig: PatienceConfig = PatienceConfig(Span(6, Seconds))
}
