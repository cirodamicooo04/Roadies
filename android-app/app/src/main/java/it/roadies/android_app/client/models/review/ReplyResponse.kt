package it.roadies.android_app.client.models.review

import java.time.LocalDateTime
import java.util.UUID

data class ReplyResponse(
    val id: UUID,
    val reviewId: UUID,
    val content: String,
    val userId: String,
    val createdAt: LocalDateTime
)