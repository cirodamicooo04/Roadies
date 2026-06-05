package it.roadies.android_app.client.models

import com.google.gson.annotations.SerializedName
import java.time.LocalDateTime

data class ErrorResponse(
    @SerializedName("timestamp")
    val timestamp: LocalDateTime? = null,

    @SerializedName("status")
    val status: Int? = null,

    @SerializedName("error")
    val error: String? = null,

    @SerializedName("message")
    val message: String? = null,

    @SerializedName("path")
    val path: String? = null
)
