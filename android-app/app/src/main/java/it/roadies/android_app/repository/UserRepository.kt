package it.roadies.android_app.repository

import it.roadies.android_app.client.apis.user.UserManagementApi
import it.roadies.android_app.client.models.user.UserProfileResponseDTO
import it.roadies.android_app.client.models.user.UserSyncRequestDTO
import it.roadies.android_app.repository.utils.ApiResponse
import it.roadies.android_app.repository.utils.safeApiCall
import javax.inject.Inject


sealed class SyncResult {
    data class NewUser(val profile: UserProfileResponseDTO) : SyncResult() // 201
    data class ExistingUser(val profile: UserProfileResponseDTO) : SyncResult() // 200
    data class Error(val message: String) : SyncResult()
}

class UserRepository @Inject constructor(
    private val userApi: UserManagementApi
) {
    suspend fun getProfile(): ApiResponse<UserProfileResponseDTO> {
        return safeApiCall {
            userApi.getProfile()
        }
    }

    suspend fun syncUser(request: UserSyncRequestDTO): ApiResponse<UserProfileResponseDTO> {
        return safeApiCall {
            userApi.syncUser(request)
        }
    }
}