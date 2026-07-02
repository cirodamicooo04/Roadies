package it.roadies.android_app.client.models.review

data class ReviewUpdateRequest(
    val rating: Int,
    val content: String
)