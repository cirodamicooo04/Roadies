package it.roadies.android_app.client.models.travel

import com.google.gson.annotations.SerializedName

data class PhotonResponse(
    val features: List<PhotonFeature>
)

data class PhotonGeometry(
    val type: String,
    val coordinates: List<Double>
)

data class PhotonFeature(
    val properties: PhotonProperties,
    val geometry: PhotonGeometry? = null
) {
    fun toSearchSuggestion(): SearchSuggestion? {
        val props = properties
        if (props.name.isNullOrBlank()) return null

        val lat = geometry?.coordinates?.getOrNull(1)
        val lon = geometry?.coordinates?.getOrNull(0)

        return when (props.osmValue) {
            "city", "town", "village", "municipality" -> SearchSuggestion(
                name = props.name, 
                type = LocationType.DESTINATION,
                country = props.country,
                countryCode = props.countryCode,
                latitude = lat,
                longitude = lon
            )
            else -> null
        }
    }

    fun toAddressSuggestion(): SearchSuggestion? {
        val props = properties
        if (props.osmValue in listOf("city", "town", "village", "municipality", "state", "country", "continent")) {
            return null
        }

        val lat = geometry?.coordinates?.getOrNull(1)
        val lon = geometry?.coordinates?.getOrNull(0)

        val addressParts = mutableListOf<String>()
        if (!props.street.isNullOrBlank()) {
            val streetWithNumber = if (!props.housenumber.isNullOrBlank()) "${props.street} ${props.housenumber}" else props.street
            addressParts.add(streetWithNumber)
        } else if (!props.name.isNullOrBlank()) {
            addressParts.add(props.name)
        }

        if (addressParts.isEmpty()) return null
        
        val fullAddress = addressParts.joinToString(", ")

        return SearchSuggestion(
            name = fullAddress,
            type = LocationType.ADDRESS,
            country = props.country,
            countryCode = props.countryCode,
            latitude = lat,
            longitude = lon
        )
    }
}

data class PhotonProperties(
    val name: String?,
    val country: String?,
    val city: String?,
    val street: String?,
    val housenumber: String?,
    
    @SerializedName("countrycode")
    val countryCode: String?,
    
    @SerializedName("osm_key")
    val osmKey: String?,
    
    @SerializedName("osm_value")
    val osmValue: String?
)
