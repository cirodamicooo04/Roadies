package it.roadies.android_app.client.apis.travel

import retrofit2.http.*
import retrofit2.Response

import it.roadies.android_app.client.models.travel.FavouriteListCreateRequest
import it.roadies.android_app.client.models.travel.FavouriteListResponse
import it.roadies.android_app.client.models.travel.FavouriteListUpdateRequest
import java.util.UUID

interface FavouriteListsManagementApi {
    /**
     * POST api/v1/favourite-lists/{listId}/activities/{activityId}
     * Aggiungi attività
     * Aggiunge una singola attività alla lista preferiti.
     * Responses:
     *  - 200: OK
     *
     * @param listId 
     * @param activityId 
     * @return [Unit]
     */
    @POST("api/v1/favourite-lists/{listId}/activities/{activityId}")
    suspend fun addActivityToList(@Path("listId") listId: UUID, @Path("activityId") activityId: UUID): Response<Unit>

    /**
     * POST api/v1/favourite-lists/{listId}/friends/{friendId}
     * Aggiungi amico alla lista
     * Autorizza un amico specifico a vedere questa lista (utile per liste SHARED_SPECIFIC).
     * Responses:
     *  - 200: OK
     *
     * @param listId 
     * @param friendId 
     * @return [Unit]
     */
    @POST("api/v1/favourite-lists/{listId}/friends/{friendId}")
    suspend fun addFriendToList(@Path("listId") listId: UUID, @Path("friendId") friendId: String): Response<Unit>

    /**
     * POST api/v1/favourite-lists/{listId}/travels/{travelId}
     * Aggiungi viaggio
     * Aggiunge un viaggio alla lista preferiti.
     * Responses:
     *  - 200: OK
     *
     * @param listId 
     * @param travelId 
     * @return [Unit]
     */
    @POST("api/v1/favourite-lists/{listId}/travels/{travelId}")
    suspend fun addTravelToList(@Path("listId") listId: UUID, @Path("travelId") travelId: UUID): Response<Unit>

    /**
     * POST api/v1/favourite-lists
     * Crea una nuova lista
     * Crea una lista preferiti vuota con una visibilità specifica.
     * Responses:
     *  - 200: OK
     *
     * @param favouriteListCreateRequest 
     * @return [FavouriteListResponse]
     */
    @POST("api/v1/favourite-lists")
    suspend fun createList(@Body favouriteListCreateRequest: FavouriteListCreateRequest): Response<FavouriteListResponse>

    /**
     * DELETE api/v1/favourite-lists/{id}
     * Elimina lista
     * Elimina definitivamente una lista e tutti i collegamenti al suo interno.
     * Responses:
     *  - 200: OK
     *
     * @param id 
     * @return [Unit]
     */
    @DELETE("api/v1/favourite-lists/{id}")
    suspend fun deleteList(@Path("id") id: UUID): Response<Unit>

    /**
     * GET api/v1/favourite-lists/{id}
     * Dettaglio lista
     * Recupera una lista e i suoi elementi. Effettua controlli di sicurezza in base alla visibilità.
     * Responses:
     *  - 200: OK
     *
     * @param id 
     * @return [FavouriteListResponse]
     */
    @GET("api/v1/favourite-lists/{id}")
    suspend fun getList(@Path("id") id: UUID): Response<FavouriteListResponse>

    /**
     * GET api/v1/favourite-lists/my-lists
     * Le mie liste
     * Recupera tutte le liste create dall&#39;utente loggato.
     * Responses:
     *  - 200: OK
     *
     * @return [kotlin.collections.List<FavouriteListResponse>]
     */
    @GET("api/v1/favourite-lists/my-lists")
    suspend fun getMyLists(): Response<List<FavouriteListResponse>>

    /**
     * DELETE api/v1/favourite-lists/{listId}/activities/{activityId}
     * Rimuovi attività
     * Rimuove una singola attività dalla lista preferiti.
     * Responses:
     *  - 200: OK
     *
     * @param listId 
     * @param activityId 
     * @return [Unit]
     */
    @DELETE("api/v1/favourite-lists/{listId}/activities/{activityId}")
    suspend fun removeActivityFromList(@Path("listId") listId: UUID, @Path("activityId") activityId: UUID): Response<Unit>

    /**
     * DELETE api/v1/favourite-lists/{listId}/friends/{friendId}
     * Rimuovi amico dalla lista
     * Revoca a un amico specifico l&#39;autorizzazione a vedere questa lista.
     * Responses:
     *  - 200: OK
     *
     * @param listId 
     * @param friendId 
     * @return [Unit]
     */
    @DELETE("api/v1/favourite-lists/{listId}/friends/{friendId}")
    suspend fun removeFriendFromList(@Path("listId") listId: UUID, @Path("friendId") friendId: String): Response<Unit>

    /**
     * DELETE api/v1/favourite-lists/{listId}/travels/{travelId}
     * Rimuovi viaggio
     * Rimuove un viaggio dalla lista preferiti.
     * Responses:
     *  - 200: OK
     *
     * @param listId 
     * @param travelId 
     * @return [Unit]
     */
    @DELETE("api/v1/favourite-lists/{listId}/travels/{travelId}")
    suspend fun removeTravelFromList(@Path("listId") listId: UUID, @Path("travelId") travelId: UUID): Response<Unit>

    @GET("api/v1/favourite-lists/users/{targetUserId}/lists")
    suspend fun getUserLists(@Path("targetUserId") targetUserId: String): Response<List<FavouriteListResponse>>

    @GET("api/v1/favourite-lists/{id}")
    suspend fun getListDetail(@Path("id") id: UUID): Response<FavouriteListResponse>

    @PUT("api/v1/favourite-lists/{id}")
    suspend fun updateList(
        @Path("id") id: UUID,
        @Body request: FavouriteListUpdateRequest
    ): retrofit2.Response<FavouriteListResponse>
}
