package it.roadies.android_app.client.models.chat

import com.google.gson.annotations.SerializedName

data class MessageRequestDTO(
    @SerializedName("conversationId")
    val conversationId: String,

    @SerializedName("content")
    val content: String
)