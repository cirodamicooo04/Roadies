package it.roadies.android_app.client.models.travel

data class SearchSuggestion(
    val name: String,
    val type: LocationType,
    val country: String? = null,
    val countryCode: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null
)
