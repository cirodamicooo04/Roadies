package it.roadies.android_app.client.apis.user

import it.roadies.android_app.client.infrastructure.CollectionFormats.*
import retrofit2.http.*
import retrofit2.Call
import okhttp3.RequestBody
import com.google.gson.annotations.SerializedName

import it.roadies.android_app.client.models.user.PendingOrganizerRequestResponseDTO
import it.roadies.android_app.client.models.user.UserProfileResponseDTO
import it.roadies.android_app.client.models.user.UserSyncRequestDTO
import it.roadies.android_app.client.models.user.UserUpdateRequestDTO
import it.roadies.android_app.client.models.user.MinimalInformationResponseDTO
import okhttp3.MultipartBody
import retrofit2.Response

interface UserManagementApi {
    /**
     * DELETE api/v1/users/delete
     * Elimina profilo
     * Rimozione definitiva dell&#39;account
     * Responses:
     *  - 200: OK
     *
     * @return [Response]<[Unit]>
     */
    @DELETE("api/v1/users/delete")
    suspend fun deleteProfile(): Response<Unit>

    /**
     * GET api/v1/users/organizer-requests/pending
     * Lista richieste organizzatore in sospeso
     * 
     * Responses:
     *  - 200: OK
     *
     * @return [Response]<[kotlin.collections.List<PendingOrganizerRequestResponseDTO>]>
     */
    @GET("api/v1/users/organizer-requests/pending")
    suspend fun getPendingOrganizerRequests(): Response<kotlin.collections.List<PendingOrganizerRequestResponseDTO>>

    /**
     * GET api/v1/users/me
     * Il mio profilo
     * Recupera i dati dell&#39;utente loggato
     * Responses:
     *  - 200: OK
     *
     * @return [Response]<[UserProfileResponseDTO]>
     */
    @GET("api/v1/users/me")
    suspend fun getProfile(): Response<UserProfileResponseDTO>

    /**
     * POST api/v1/users/request-organizer
     * Richiedi ruolo organizzatore
     * 
     * Responses:
     *  - 200: OK
     *
     * @return [Response]<[Unit]>
     */
    @POST("api/v1/users/request-organizer")
    suspend fun requestOrganizerRole(): Response<Unit>

    /**
     * PATCH api/v1/users/admin/review-organizer/{targetUserId}
     * Approva o rifiuta richiesta organizzatore
     * 
     * Responses:
     *  - 200: OK
     *
     * @param targetUserId 
     * @param approved 
     * @param reason  (optional)
     * @return [Response]<[Unit]>
     */
    @PATCH("api/v1/users/admin/review-organizer/{targetUserId}")
    suspend fun reviewOrganizerRequest(@Path("targetUserId") targetUserId: kotlin.String, @Query("approved") approved: kotlin.Boolean, @Query("reason") reason: kotlin.String? = null): Response<Unit>

    /**
     * GET api/v1/users/search
     * Cerca utente
     * Ricerca pubblica di un profilo
     * Responses:
     *  - 200: OK
     *
     * @param username 
     * @return [Response]<[UserProfileResponseDTO]>
     */
    @GET("api/v1/users/public/search")
    suspend fun searchUser(@Query("username") username: kotlin.String): Response<List<UserProfileResponseDTO>>

    /**
     * POST api/v1/users/sync
     * Sincronizza Utente
     * Crea o aggiorna il profilo dell&#39;utente al login
     * Responses:
     *  - 200: OK
     *
     * @param userSyncRequestDTO 
     * @return [Response]<[UserProfileResponseDTO]>
     */
    @POST("api/v1/users/sync")
    suspend fun syncUser(@Body userSyncRequestDTO: UserSyncRequestDTO): Response<UserProfileResponseDTO>

    /**
     * PUT api/v1/users/update
     * Aggiorna profilo
     * Modifica i dati del proprio profilo
     * Responses:
     *  - 200: OK
     *
     * @param userUpdateRequestDTO 
     * @return [Response]<[UserProfileResponseDTO]>
     */
    @PUT("api/v1/users/update")
    suspend fun updateProfile(@Body userUpdateRequestDTO: UserUpdateRequestDTO): Response<UserProfileResponseDTO>

    /**
     * POST api/v1/users/minimal-info
     * Recupera info minime
     * Restituisce ID, username e avatar per una lista di ID
     * Responses:
     *  - 200: OK
     *
     * @param userIds
     * @return [Response]<[kotlin.collections.List<MinimalInformationResponseDTO>]>
     */
    @POST("api/v1/users/public/minimal-info")
    suspend fun getMinimalInformation(@Body userIds: List<String>): Response<List<MinimalInformationResponseDTO>>


    @GET("api/v1/users/public/{username}/minimal-info")
    suspend fun getUserMinimalInformation(@Path("username") userId: String): Response<MinimalInformationResponseDTO>

    @Multipart
    @POST("api/v1/users/avatar")
    suspend fun uploadAvatar(
        @Part avatarFile: MultipartBody.Part
    ): Response<UserProfileResponseDTO>

    @GET("api/v1/users/public/{username}/is-organizer")
    suspend fun checkIsOrganizer(@Path("username") username: String): Response<Boolean>

    @GET("api/v1/users/public/id/{username}")
    suspend fun getUserIdByUsername(@Path("username") username: String): Response<String>


}
