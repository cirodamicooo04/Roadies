package it.roadies.android_app.repository

import it.roadies.android_app.client.apis.travel.ViaggiApi
import it.roadies.android_app.client.models.travel.TravelResponse
import it.roadies.android_app.client.models.travel.TravelSummaryResponse
import retrofit2.Response
import java.util.UUID
import javax.inject.Inject

class TravelRepository @Inject constructor(private val viaggiApi: ViaggiApi){
    suspend fun getRecommendations(): Response<List<TravelSummaryResponse>>{
        return viaggiApi.getRecommendations()
    }

    suspend fun getTravelById(id: UUID): Response<TravelResponse> {
        return viaggiApi.getTravelById(id)
    }
}