package it.roadies.android_app.repository

import it.roadies.android_app.client.apis.travel.PhotonApi
import it.roadies.android_app.client.models.travel.SearchSuggestion
import javax.inject.Inject

class LocationRepository @Inject constructor(
    private val photonApi: PhotonApi
) {
    suspend fun getSuggestions(query: String): List<SearchSuggestion> {
        return try {
            val response = photonApi.getSuggestions(query = query)
            if (response.isSuccessful) {
                val apiResults = response.body()?.features
                    ?.mapNotNull { it.properties.toSearchSuggestion() } 
                    ?: emptyList()
                
                // Rimuoviamo i cloni
                apiResults.distinctBy { it.name }
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            android.util.Log.e("LocationRepository", "Error fetching from Photon", e)
            emptyList()
        }
    }
}
