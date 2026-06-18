package it.roadies.android_app.repository

import it.roadies.android_app.client.apis.travel.AttivitApi
import javax.inject.Inject

class ActivityRepository @Inject constructor(
    private val activityApi: AttivitApi,
    private val viaggiApi: it.roadies.android_app.client.apis.travel.ViaggiApi
) {
    suspend fun searchActivities(
        destination: String? = null,
        minPrice: java.math.BigDecimal? = null,
        maxPrice: java.math.BigDecimal? = null,
        minDurationDays: Int? = null,
        maxDurationDays: Int? = null,
        continent: it.roadies.android_app.client.apis.travel.ViaggiApi.ContinentSearchTravels? = null,
        country: String? = null,
        page: Int = 0
    ): it.roadies.android_app.repository.utils.ApiResponse<it.roadies.android_app.client.models.travel.PageResponse<it.roadies.android_app.client.models.travel.ActivitySummaryResponse>> {
        return it.roadies.android_app.repository.utils.safeApiCall {
            viaggiApi.searchActivitiesTyped(
                destination = destination,
                minPrice = minPrice,
                maxPrice = maxPrice,
                minDurationDays = minDurationDays,
                maxDurationDays = maxDurationDays,
                continent = continent,
                country = country,
                page = page,
                type = "ACTIVITY"
            )
        }
    }
}