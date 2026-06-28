package it.roadies.android_app.repository

import it.roadies.android_app.client.apis.travel.AttivitApi
import it.roadies.android_app.client.apis.travel.ViaggiApi
import it.roadies.android_app.client.models.travel.ActivityResponse
import it.roadies.android_app.client.models.travel.ActivitySummaryResponse
import it.roadies.android_app.client.models.travel.PageResponse
import it.roadies.android_app.repository.utils.ApiResponse
import it.roadies.android_app.repository.utils.safeApiCall
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

    suspend fun getActivityById(id: java.util.UUID): ApiResponse<ActivityResponse> {
        return safeApiCall {
            activityApi.getActivity(id)
        }
    }

    suspend fun getActivityDepartures(activityId: java.util.UUID): ApiResponse<List<it.roadies.android_app.client.models.travel.ActivityDepartureResponse>> {
        return safeApiCall {
            activityApi.getDepartures1(activityId)
        }
    }
}