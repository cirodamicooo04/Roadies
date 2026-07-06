package it.roadies.android_app.repository

import it.roadies.android_app.client.apis.travel.FavouriteListsManagementApi
import it.roadies.android_app.client.models.travel.FavouriteListCreateRequest
import it.roadies.android_app.client.models.travel.FavouriteListResponse
import it.roadies.android_app.client.models.travel.FavouriteListUpdateRequest
import it.roadies.android_app.repository.utils.ApiResponse
import it.roadies.android_app.repository.utils.safeApiCall
import java.util.UUID
import javax.inject.Inject

class FavouriteRepository @Inject constructor(
    private val api: FavouriteListsManagementApi
) {
    suspend fun getMyLists(): ApiResponse<List<FavouriteListResponse>> {
        return safeApiCall { api.getMyLists() }
    }

    suspend fun createList(request: FavouriteListCreateRequest): ApiResponse<FavouriteListResponse> {
        return safeApiCall { api.createList(request) }
    }

    suspend fun addTravelToList(listId: UUID, travelId: UUID): ApiResponse<Unit> {
        return safeApiCall { api.addTravelToList(listId, travelId) }
    }

    suspend fun addActivityToList(listId: UUID, activityId: UUID): ApiResponse<Unit> {
        return safeApiCall { api.addActivityToList(listId, activityId) }
    }

    suspend fun removeTravelFromList(listId: UUID, travelId: UUID): ApiResponse<Unit> {
        return safeApiCall { api.removeTravelFromList(listId, travelId) }
    }

    suspend fun removeActivityFromList(listId: UUID, activityId: UUID): ApiResponse<Unit> {
        return safeApiCall { api.removeActivityFromList(listId, activityId) }
    }

    suspend fun addFriendToList(listId: UUID, friendId: String): ApiResponse<Unit> {
        return safeApiCall { api.addFriendToList(listId, friendId) }
    }

    suspend fun removeFriendFromList(listId: UUID, friendId: String): ApiResponse<Unit> {
        return safeApiCall { api.removeFriendFromList(listId, friendId) }
    }

    suspend fun deleteList(listId: UUID): ApiResponse<Unit> {
        return safeApiCall { api.deleteList(listId) }
    }

    suspend fun getListDetail(listId: UUID): ApiResponse<FavouriteListResponse> {
        return safeApiCall { api.getList(listId) }
    }

    suspend fun getUserLists(targetUserId: String): ApiResponse<List<FavouriteListResponse>> {
        return safeApiCall { api.getUserLists(targetUserId) }
    }


    suspend fun updateList(listId: UUID, request: FavouriteListUpdateRequest): ApiResponse<FavouriteListResponse> {
        return safeApiCall { api.updateList(listId, request) }
    }

}
