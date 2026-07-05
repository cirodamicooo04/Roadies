package it.roadies.android_app.client.models.review

import java.time.LocalDateTime
import java.util.UUID

data class ReviewResponse(
    val id: UUID,
    val travelId: UUID,
    val reviewType: String,
    val userId: String,
    val rating: Int,
    val content: String,
    val createdAt: LocalDateTime,
    val reply: ReplyResponse?
)