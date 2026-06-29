package it.roadies.android_app.repository

import it.roadies.android_app.client.apis.user.FriendshipManagementApi
import it.roadies.android_app.client.models.user.FriendshipResponseDTO
import it.roadies.android_app.client.models.user.UserProfileResponseDTO
import it.roadies.android_app.repository.utils.ApiResponse
import it.roadies.android_app.repository.utils.safeApiCall
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FriendshipRepository @Inject constructor(
    private val friendshipApi: FriendshipManagementApi
) {
    suspend fun getFriendsList(): ApiResponse<List<UserProfileResponseDTO>> {
        return safeApiCall {
            friendshipApi.getFriends()
        }
    }

    suspend fun sendFriendshipRequest(receiverUsername: String): ApiResponse<Unit>{
        return safeApiCall {
            friendshipApi.send(receiverUsername)
        }
    }

    suspend fun getSentRequests(): ApiResponse<List<FriendshipResponseDTO>> {
        return safeApiCall {
            friendshipApi.getSentRequests()
        }
    }

    suspend fun respondToRequest(friendshipId: UUID, status: FriendshipManagementApi.StatusRespond): ApiResponse<Unit> {
        return safeApiCall {
            friendshipApi.respond(friendshipId, status)
        }
    }

    suspend fun getPendingRequests(): ApiResponse<List<FriendshipResponseDTO>> {
        return safeApiCall {
            friendshipApi.getPendingRequests()
        }
    }
}