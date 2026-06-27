package it.roadies.android_app.client.models.booking

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

data class BookingHomeResponse(
    @SerializedName("bookingId")
    val bookingId: UUID? = null,
    @SerializedName("principalId")
    val principalId: UUID? = null,
    @SerializedName("travelName")
    val travelName: String? = null,
    @SerializedName("peopleCount")
    val peopleCount: Int? = null,
    @SerializedName("totalPrice")
    val totalPrice: BigDecimal? = null,
    @SerializedName("startDate")
    val startDate: LocalDateTime? = null,
    @SerializedName("endDate")
    val endDate: LocalDateTime? = null,
    @SerializedName("departureType")
    val departureType: DepartureType? = null
    )
