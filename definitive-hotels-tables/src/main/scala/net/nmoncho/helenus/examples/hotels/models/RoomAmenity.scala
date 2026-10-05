package net.nmoncho.helenus.examples.hotels.models

/** A row of `amenities_by_room` (Q5): amenities denormalized by room. */
final case class RoomAmenity(
    hotelId: String,
    roomNumber: Short,
    amenityName: String,
    description: String
)
