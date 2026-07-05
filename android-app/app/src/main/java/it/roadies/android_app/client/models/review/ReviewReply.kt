package it.roadies.android_app.client.models.review

import java.util.UUID

// This maps to the ReviewReply entity structure expected in PUT updates
data class ReviewReply(
    val id: UUID,
    val reviewId: UUID,
    val content: String,
    val userId: String
)