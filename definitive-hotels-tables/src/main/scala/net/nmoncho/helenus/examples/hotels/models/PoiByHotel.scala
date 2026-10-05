package net.nmoncho.helenus.examples.hotels.models

/** A row of `pois_by_hotel` (Q3): points of interest denormalized by hotel. */
final case class PoiByHotel(
    hotelId: String,
    poiName: String,
    description: String
)
