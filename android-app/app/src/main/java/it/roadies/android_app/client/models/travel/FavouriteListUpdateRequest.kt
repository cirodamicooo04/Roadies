package it.roadies.android_app.client.models.travel

data class FavouriteListUpdateRequest(
    val name: String,
    val visibility: FavouriteListResponse.Visibility
)
