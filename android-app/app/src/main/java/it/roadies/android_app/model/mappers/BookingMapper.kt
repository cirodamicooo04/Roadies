package it.roadies.android_app.model.mappers

import it.roadies.android_app.client.models.booking.BookingHomeResponse
import it.roadies.android_app.model.Booking
import java.math.BigDecimal
import java.time.LocalDateTime

fun BookingHomeResponse.toEntity(): Booking {
    return Booking (
        id = this.bookingId?: throw IllegalArgumentException("bookingId mancante dal server"),
        principalId = this.principalId,
        totalPrice = this.totalPrice?: BigDecimal(0.0),
        startDate = this.startDate?: LocalDateTime.now(),
        endDate = this.endDate?: LocalDateTime.now(),
        title = this.travelName?: "without name",
        peopleCount = this.peopleCount?: 1,
        departureType = this.departureType,
    )
}

fun Booking.toHomeResponse(): BookingHomeResponse {
    return BookingHomeResponse(
        bookingId = this.id,
        principalId = this.principalId,
        travelName = this.title,
        peopleCount = this.peopleCount,
        totalPrice = this.totalPrice,
        startDate = this.startDate,
        endDate = this.endDate,
        departureType = this.departureType,
    )
}
