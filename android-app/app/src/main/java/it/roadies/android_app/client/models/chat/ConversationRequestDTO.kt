package it.roadies.android_app.client.models.chat

import com.google.gson.annotations.SerializedName

data class ConversationRequestDTO(
    @SerializedName("travelerId")
    val travelerId: String,

    @SerializedName("organizerId")
    val organizerId: String
)