package it.roadies.android_app.client.models.chat

import com.google.gson.annotations.SerializedName

data class ConversationResponseDTO(
    @SerializedName("id")
    val id: String,

    @SerializedName("travelerId")
    val travelerId: String,

    @SerializedName("organizerId")
    val organizerId: String,

    @SerializedName("createdAt")
    val createdAt: String? = null
)