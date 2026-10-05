package net.nmoncho.helenus.examples.hotels.models

import java.time.LocalDate

/** A row of `available_rooms_by_hotel_date` (Q4). */
final case class AvailableRoom(
    hotelId: String,
    date: LocalDate,
    roomNumber: Short,
    isAvailable: Boolean
)
