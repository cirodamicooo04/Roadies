package it.roadies.android_app.client.models.review

data class ReviewRequest(
    val rating: Int,
    val content: String,
    val reviewType: String // Representing enum ReviewType
)