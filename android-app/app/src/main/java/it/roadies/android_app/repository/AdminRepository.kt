package it.roadies.android_app.repository

import it.roadies.android_app.client.apis.admin.AdminManagementApi
import it.roadies.android_app.client.models.user.PendingOrganizerRequestResponseDTO
import it.roadies.android_app.client.models.user.UserResponseDTO
import it.roadies.android_app.repository.utils.ApiResponse
import it.roadies.android_app.repository.utils.safeApiCall
import javax.inject.Inject

class AdminRepository @Inject constructor(
    private val adminApi: AdminManagementApi
){

    suspend fun blockUser(id: String) = safeApiCall {adminApi.blockUser(id)}

    suspend fun unblockUser(id: String) = safeApiCall {adminApi.unblockUser(id)}

    suspend fun reviewOrganizerRequest(targetKeycloakId: String,  approved: Boolean, reason: String?) = safeApiCall{adminApi.reviewOrganizerRequest(targetKeycloakId, approved, reason)}

    suspend fun getPendingOrganizerRequests(): ApiResponse<List<PendingOrganizerRequestResponseDTO>> {
        return safeApiCall { adminApi.getPendingOrganizerRequests() }
    }

    suspend fun getUsersByFilter(filter: String): ApiResponse<List<UserResponseDTO>>{
        return safeApiCall {adminApi.getUsersByFilter(filter)}
    }
}