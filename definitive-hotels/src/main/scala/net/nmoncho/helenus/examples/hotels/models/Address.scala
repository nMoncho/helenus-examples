package net.nmoncho.helenus.examples.hotels.models

import net.nmoncho.helenus.api.Udt
import com.datastax.oss.driver.api.core.`type`.codec.TypeCodec

@Udt
final case class Address(
    street: String,
    city: String,
    stateOrProvince: String,
    postalCode: String,
    country: String
)

object Address {
  import net.nmoncho.helenus._

  implicit val typeCodec: TypeCodec[Address] = Codec.udtOf[Address]
}
