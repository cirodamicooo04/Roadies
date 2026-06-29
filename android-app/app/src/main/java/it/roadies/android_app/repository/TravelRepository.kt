package it.roadies.android_app.repository

import it.roadies.android_app.client.apis.travel.ViaggiApi
import it.roadies.android_app.client.models.travel.OrganizerTravelsActivityResponse
import it.roadies.android_app.client.models.travel.PageResponse
import it.roadies.android_app.client.models.travel.TravelDepartureResponse
import it.roadies.android_app.client.models.travel.TravelResponse
import it.roadies.android_app.client.models.travel.TravelSummaryResponse
import it.roadies.android_app.repository.utils.ApiResponse
import it.roadies.android_app.repository.utils.safeApiCall
import java.math.BigDecimal
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

    suspend fun searchTravels(
        destination: String? = null,
        minPrice: BigDecimal? = null,
        maxPrice: BigDecimal? = null,
        minDurationDays: Int? = null,
        maxDurationDays: Int? = null,
        continent: ViaggiApi.ContinentSearchTravels? = null,
        country: String? = null,
        page: Int = 0,
        sort: List<String>? = null
    ): ApiResponse<PageResponse<TravelSummaryResponse>> {
        return safeApiCall {
            viaggiApi.searchTravelsTyped(
                destination = destination,
                minPrice = minPrice,
                maxPrice = maxPrice,
                minDurationDays = minDurationDays,
                maxDurationDays = maxDurationDays,
                continent = continent,
                country = country,
                page = page,
                sort = sort,
                type = "TRAVEL"
            )
        }
    }

    suspend fun getTravelDepartures(travelId: UUID): ApiResponse<List<TravelDepartureResponse>>{
        return safeApiCall { viaggiApi.getDepartures(travelId) }
    }

    suspend fun getPublicRecommendations(): ApiResponse<List<TravelSummaryResponse>>{
        return safeApiCall { viaggiApi.getPublicRecommendations() }
    }

    suspend fun getOrganizerTravelsActivities(): ApiResponse<OrganizerTravelsActivityResponse>{
        return safeApiCall { viaggiApi.getMyTravels() }
    }

    suspend fun deleteTravel(travelId: UUID): ApiResponse<Any> {
        return safeApiCall { viaggiApi.deleteTravel(travelId) }
    }

    suspend fun uploadImage(file: okhttp3.MultipartBody.Part): ApiResponse<it.roadies.android_app.client.models.travel.ImageResponse> {
        return safeApiCall { viaggiApi.uploadImage(file) }
    }

    suspend fun createTravel(request: it.roadies.android_app.client.models.travel.TravelCreateRequest): ApiResponse<TravelResponse> {
        return safeApiCall { viaggiApi.createTravel(request) }
    }
}
