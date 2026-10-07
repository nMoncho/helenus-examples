package net.nmoncho.helenus.examples.hotels.repositories

import net.nmoncho.helenus.examples.hotels.HotelsTestData
import net.nmoncho.helenus.examples.hotels.LocalSpark
import org.apache.spark.SparkContext
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class HotelRepositorySpec extends AnyWordSpec with Matchers with CassandraSpec {

  private def sc: SparkContext = LocalSpark.session.sparkContext

  "HotelRepository (Spark)" should {
    "write hotels with the CQL-first sink and read them back via cassandraTable" in {
      val hotels = HotelsTestData.Hotels.all

      HotelRepository.save(sc.parallelize(hotels))

      val read = HotelRepository.all(sc).collect().toList

      withClue("every row maps back, including the frozen `address` UDT and the `pois` set") {
        read should contain theSameElementsAs hotels
        read.find(_.id == "h1").map(_.address) shouldBe Some(HotelsTestData.Hotels.h1.address)
        read.find(_.id == "h1").map(_.pois) shouldBe Some(HotelsTestData.Hotels.h1.pois)
      }
    }

    "respect an LWT (IF NOT EXISTS) write" in {
      val h1 = HotelsTestData.Hotels.h1
      HotelRepository.save(sc.parallelize(Seq(h1)))

      // Try to overwrite h1 (should not win) and insert a brand-new h11, both IF NOT EXISTS.
      val changedH1 = h1.copy(name = "SHOULD NOT WIN")
      val newH11    = HotelsTestData.Hotels.h11
      HotelRepository.saveIfNotExists(sc.parallelize(Seq(changedH1, newH11)))

      val byId = HotelRepository.all(sc).collect().map(h => h.id -> h.name).toMap
      byId.get(h1.id) shouldBe Some(h1.name) // existing row preserved
      byId.get(newH11.id) shouldBe Some(newH11.name) // new row applied
    }
  }

  override def beforeAll(): Unit = {
    super.beforeAll()
    executeFile("hotels.cql")
  }
}
