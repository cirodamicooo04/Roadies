package it.roadies.android_app.repository

import it.roadies.android_app.client.apis.travel.AttivitApi
import it.roadies.android_app.client.apis.travel.ViaggiApi
import it.roadies.android_app.client.models.travel.ActivityCreateRequest
import it.roadies.android_app.client.models.travel.ActivityDepartureResponse
import it.roadies.android_app.client.models.travel.ActivityResponse
import it.roadies.android_app.client.models.travel.ActivitySummaryResponse
import it.roadies.android_app.client.models.travel.ActivityUpdateRequest
import it.roadies.android_app.client.models.travel.ActivityDepartureCreateRequest
import it.roadies.android_app.client.models.travel.ActivityDepartureUpdateRequest
import it.roadies.android_app.client.models.travel.ImageResponse
import it.roadies.android_app.client.models.travel.PageResponse
import it.roadies.android_app.repository.utils.ApiResponse
import it.roadies.android_app.repository.utils.safeApiCall
import okhttp3.MultipartBody
import java.math.BigDecimal
import java.util.UUID
import javax.inject.Inject

class ActivityRepository @Inject constructor(
    private val activityApi: AttivitApi,
    private val viaggiApi: ViaggiApi
) {
    suspend fun searchActivities(
        destination: String? = null,
        minPrice: java.math.BigDecimal? = null,
        maxPrice: java.math.BigDecimal? = null,
        minDurationDays: Int? = null,
        maxDurationDays: Int? = null,
        continent: ViaggiApi.ContinentSearchTravels? = null,
        country: String? = null,
        page: Int = 0,
        sort: List<String>? = null
    ): ApiResponse<PageResponse<ActivitySummaryResponse>> {
        return safeApiCall {
            viaggiApi.searchActivitiesTyped(
                destination = destination,
                minPrice = minPrice,
                maxPrice = maxPrice,
                minDurationDays = minDurationDays,
                maxDurationDays = maxDurationDays,
                continent = continent,
                country = country,
                page = page,
                sort = sort,
                type = "ACTIVITY"
            )
        }
    }

    suspend fun getActivityById(id: UUID): ApiResponse<ActivityResponse> {
        return safeApiCall {
            activityApi.getActivity(id)
        }
    }

    suspend fun getActivityDepartures(activityId: UUID): ApiResponse<List<ActivityDepartureResponse>> {
        return safeApiCall {
            activityApi.getDepartures1(activityId)
        }
    }

    suspend fun deleteActivity(activityId: UUID): ApiResponse<Any> {
        return safeApiCall { activityApi.deleteActivity1(id = activityId) }
    }

    suspend fun uploadImage(file: MultipartBody.Part): ApiResponse<ImageResponse> {
        return safeApiCall {
            viaggiApi.uploadImage(file)
        }
    }

    suspend fun createActivity(activityCreateRequest: ActivityCreateRequest): ApiResponse<ActivityResponse> {
        return safeApiCall {
            activityApi.createActivity(activityCreateRequest)
        }
    }

    suspend fun updateActivity(activityId: UUID, activityUpdateRequest: ActivityUpdateRequest): ApiResponse<ActivityResponse> {
        return safeApiCall {
            activityApi.updateActivity1(activityId, activityUpdateRequest)
        }
    }

    suspend fun addDeparture(activityId: UUID, request: ActivityDepartureCreateRequest): ApiResponse<ActivityDepartureResponse> {
        return safeApiCall {
            activityApi.addDeparture1(activityId, request)
        }
    }

    suspend fun updateDeparture(activityId: UUID, departureId: UUID, request: ActivityDepartureUpdateRequest): ApiResponse<ActivityDepartureResponse> {
        return safeApiCall {
            activityApi.updateDeparture1(activityId, departureId, request)
        }
    }

    suspend fun deleteDeparture(activityId: UUID, departureId: UUID): ApiResponse<Any> {
        return safeApiCall {
            activityApi.deleteDeparture1(activityId, departureId)
        }
    }

    suspend fun confirmDeparture(activityId: UUID, departureId: UUID): ApiResponse<ActivityDepartureResponse> {
        return safeApiCall {
            activityApi.confirmDeparture1(activityId, departureId)
        }
    }
}