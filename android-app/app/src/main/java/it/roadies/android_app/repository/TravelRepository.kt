package it.roadies.android_app.repository

import it.roadies.android_app.client.apis.travel.ViaggiApi
import it.roadies.android_app.client.apis.travel.AttivitApi
import it.roadies.android_app.client.models.travel.ActivityCreateRequest
import it.roadies.android_app.client.models.travel.ActivityUpdateRequest
import it.roadies.android_app.client.models.travel.ImageResponse
import it.roadies.android_app.client.models.travel.OrganizerTravelsActivityResponse
import it.roadies.android_app.client.models.travel.PageResponse
import it.roadies.android_app.client.models.travel.TravelCreateRequest
import it.roadies.android_app.client.models.travel.TravelDepartureCreateRequest
import it.roadies.android_app.client.models.travel.TravelDepartureResponse
import it.roadies.android_app.client.models.travel.TravelDepartureUpdateRequest
import it.roadies.android_app.client.models.travel.TravelResponse
import it.roadies.android_app.client.models.travel.TravelSummaryResponse
import it.roadies.android_app.client.models.travel.TravelUpdateRequest
import it.roadies.android_app.repository.utils.ApiResponse
import it.roadies.android_app.repository.utils.safeApiCall
import okhttp3.MultipartBody
import java.math.BigDecimal
import java.util.UUID
import javax.inject.Inject

class TravelRepository @Inject constructor(
    private val viaggiApi: ViaggiApi,
    private val attivitApi: AttivitApi
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

    suspend fun uploadImage(file: MultipartBody.Part): ApiResponse<ImageResponse> {
        return safeApiCall { viaggiApi.uploadImage(file) }
    }

    suspend fun createTravel(request: TravelCreateRequest): ApiResponse<TravelResponse> {
        return safeApiCall { viaggiApi.createTravel(request) }
    }

    suspend fun updateTravel (id: UUID, travelUpdateRequest: TravelUpdateRequest): ApiResponse<TravelResponse> {
        return safeApiCall { viaggiApi.updateTravel(id = id, travelUpdateRequest = travelUpdateRequest) }
    }

    suspend fun createActivity(travelId: UUID, request: ActivityCreateRequest): ApiResponse<TravelResponse> {
        return safeApiCall { viaggiApi.addActivity(travelId, request) }
    }

    suspend fun updateActivity(travelId: UUID, activityId: UUID, request: ActivityUpdateRequest): ApiResponse<TravelResponse> {
        return safeApiCall { viaggiApi.updateActivity(travelId, activityId, request) }
    }

    suspend fun deleteActivity(travelId: UUID, activityId: UUID): ApiResponse<Any> {
        return safeApiCall { viaggiApi.deleteActivity(travelId, activityId) }
    }

    suspend fun addDeparture(travelId: UUID, request: TravelDepartureCreateRequest): ApiResponse<TravelDepartureResponse> {
        return safeApiCall { viaggiApi.addDeparture(travelId, request) }
    }

    suspend fun updateDeparture(travelId: UUID, departureId: UUID, request: TravelDepartureUpdateRequest): ApiResponse<TravelDepartureResponse> {
        return safeApiCall { viaggiApi.updateDeparture(travelId, departureId, request) }
    }

    suspend fun deleteDeparture(travelId: UUID, departureId: UUID): ApiResponse<Any> {
        return safeApiCall { viaggiApi.deleteDeparture(travelId, departureId) }
    }

    suspend fun confirmDeparture(travelId: UUID, departureId: UUID): ApiResponse<TravelDepartureResponse> {
        return safeApiCall { viaggiApi.confirmDeparture(travelId, departureId) }
    }
}
