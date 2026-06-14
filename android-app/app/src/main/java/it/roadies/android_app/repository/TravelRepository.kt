package it.roadies.android_app.repository

import it.roadies.android_app.client.apis.travel.ViaggiApi
import it.roadies.android_app.client.models.travel.TravelResponse
import it.roadies.android_app.client.models.travel.TravelSummaryResponse
import it.roadies.android_app.repository.utils.ApiResponse
import it.roadies.android_app.repository.utils.safeApiCall
import java.util.UUID
import javax.inject.Inject

class TravelRepository @Inject constructor(
    private val viaggiApi: ViaggiApi
) {
    suspend fun getRecommendations(): ApiResponse<List<TravelSummaryResponse>>{
        return safeApiCall {
            viaggiApi.getRecommendations()
        }
    }

    suspend fun getTravelById(id: UUID): ApiResponse<TravelResponse> {
        return safeApiCall {
            viaggiApi.getTravelById(id)
        }
    }
}
