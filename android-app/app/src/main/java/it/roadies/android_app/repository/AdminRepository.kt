package it.roadies.android_app.repository

import it.roadies.android_app.client.apis.admin.AdminManagementApi
import it.roadies.android_app.client.models.user.PendingOrganizerRequestResponseDTO
import it.roadies.android_app.client.models.user.UserResponseDTO
import javax.inject.Inject

class AdminRepository @Inject constructor(
    private val adminApi: AdminManagementApi
){

    suspend fun blockUser(id: String) = adminApi.blockUser(id)

    suspend fun unblockUser(id: String) = adminApi.unblockUser(id)

    suspend fun reviewOrganizerRequest(targetKeycloakId: String,  approved: Boolean, reason: String?) = adminApi.reviewOrganizerRequest(targetKeycloakId, approved, reason)

    suspend fun getPendingOrganizerRequests(): List<PendingOrganizerRequestResponseDTO>{
        return adminApi.getPendingOrganizerRequests()
    }

    suspend fun getUsersByFilter(filter: String): List<UserResponseDTO>{
        return adminApi.getUsersByFilter(filter)
    }
}