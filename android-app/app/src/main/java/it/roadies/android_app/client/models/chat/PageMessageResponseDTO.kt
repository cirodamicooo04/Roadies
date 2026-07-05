package it.roadies.android_app.client.models.chat

import com.google.gson.annotations.SerializedName

data class PageMessageResponseDTO(
    @SerializedName("content")
    val content: List<MessageResponseDTO>,

    @SerializedName("pageable")
    val pageable: Any? = null,

    @SerializedName("totalElements")
    val totalElements: Long,

    @SerializedName("totalPages")
    val totalPages: Int,

    @SerializedName("last")
    val last: Boolean,

    @SerializedName("size")
    val size: Int,

    @SerializedName("number")
    val number: Int,

    @SerializedName("empty")
    val empty: Boolean
)