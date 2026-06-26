package it.roadies.android_app.repository

import it.roadies.android_app.client.apis.travel.MetadatiApi
import it.roadies.android_app.client.models.travel.TagResponse
import it.roadies.android_app.repository.utils.ApiResponse
import it.roadies.android_app.repository.utils.safeApiCall
import javax.inject.Inject

class MetadataRepository @Inject constructor(private val metadataApi: MetadatiApi) {
    suspend fun getTags(): ApiResponse<List<TagResponse>> {
        return safeApiCall { metadataApi.getTags() }
    }
}