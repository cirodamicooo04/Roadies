package it.roadies.android_app.client.models.booking

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class BookingHomeResponse(
    @SerializedName("travelName")
    val travelName: String? = null,
    @SerializedName("peopleCount")
    val peopleCount: Int? = null,
    @SerializedName("totalPrice")
    val totalPrice: BigDecimal? = null
)
