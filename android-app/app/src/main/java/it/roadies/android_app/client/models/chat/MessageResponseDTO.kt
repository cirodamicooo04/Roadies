package it.roadies.android_app.client.models.chat

import com.google.gson.annotations.SerializedName

data class MessageResponseDTO(
    @SerializedName("id")
    val id: String,

    @SerializedName("conversationId")
    val conversationId: String,

    @SerializedName("senderId")
    val senderId: String,

    @SerializedName("content")
    val content: String,

    @SerializedName("timestamp")
    val timestamp: String,

    @SerializedName("isRead")
    val isRead: Boolean
)