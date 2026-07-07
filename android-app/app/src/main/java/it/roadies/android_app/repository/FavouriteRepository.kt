package it.roadies.android_app.repository

import it.roadies.android_app.client.apis.travel.FavouriteListsManagementApi
import it.roadies.android_app.client.models.travel.FavouriteListCreateRequest
import it.roadies.android_app.client.models.travel.FavouriteListResponse
import it.roadies.android_app.client.models.travel.FavouriteListUpdateRequest
import it.roadies.android_app.model.FavouriteList
import it.roadies.android_app.model.dao.FavouriteListDao
import it.roadies.android_app.repository.utils.ApiResponse
import it.roadies.android_app.repository.utils.safeApiCall
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject

class FavouriteRepository @Inject constructor(
    private val api: FavouriteListsManagementApi,
    private val favouriteListDao: FavouriteListDao
) {

    fun observeMyLists(ownerId: String): Flow<List<FavouriteList>> =
        favouriteListDao.observeByOwner(ownerId)

    suspend fun refreshMyLists(ownerId: String): ApiResponse<List<FavouriteListResponse>> {
        val response = safeApiCall { api.getMyLists() }
        if (response.success && response.data != null) {
            val entities = response.data.mapNotNull { dto ->
                val id = dto.id ?: return@mapNotNull null
                FavouriteList(
                    id = id,
                    ownerId = ownerId,
                    name = dto.name ?: "",
                    length = dto.items?.size ?: 0,
                    visibility = dto.visibility?.value ?: "PRIVATE"
                )
            }
            favouriteListDao.deleteByOwner(ownerId)
            favouriteListDao.insertAll(entities)
        }
        return response
    }

    suspend fun clearLocalLists() {
        favouriteListDao.clearAll()
    }

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
