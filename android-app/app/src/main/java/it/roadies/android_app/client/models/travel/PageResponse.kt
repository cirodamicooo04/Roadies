package it.roadies.android_app.client.models.travel

import com.google.gson.annotations.SerializedName

data class PageResponse<T>(
    @SerializedName("content")
    val content: List<T> = emptyList(),

    @SerializedName("last")
    val last: Boolean = false,

    @SerializedName("totalElements")
    val totalElements: Long = 0,

    @SerializedName("totalPages")
    val totalPages: Int = 0
)
