package net.nmoncho.helenus.examples.hotels.models

/** A row of `hotels_by_poi` (Q1): hotels denormalized by point of interest. */
final case class HotelByPoi(
    poiName: String,
    hotelId: String,
    name: String,
    phone: String,
    address: Address
)
