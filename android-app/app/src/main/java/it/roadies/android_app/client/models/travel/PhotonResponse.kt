package it.roadies.android_app.client.models.travel

import com.google.gson.annotations.SerializedName

data class PhotonResponse(
    val features: List<PhotonFeature>
)

data class PhotonFeature(
    val properties: PhotonProperties
)

data class PhotonProperties(
    val name: String?,
    val country: String?,
    val city: String?,
    
    @SerializedName("osm_key")
    val osmKey: String?,
    
    @SerializedName("osm_value")
    val osmValue: String?
) {
    fun toSearchSuggestion(): SearchSuggestion? {
        if (name.isNullOrBlank()) return null

        return when (osmValue) {
            "country" -> SearchSuggestion(name = name, type = LocationType.COUNTRY)
            "city", "town", "village", "municipality" -> SearchSuggestion(name = name, type = LocationType.DESTINATION)
            else -> null
        }
    }
}
