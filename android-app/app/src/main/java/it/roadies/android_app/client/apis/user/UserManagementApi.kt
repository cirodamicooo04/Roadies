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

interface UserManagementApi {
    /**
     * DELETE api/v1/users/delete
     * Elimina profilo
     * Rimozione definitiva dell&#39;account
     * Responses:
     *  - 200: OK
     *
     * @return [Call]<[Unit]>
     */
    @DELETE("api/v1/users/delete")
    suspend fun deleteProfile(): Call<Unit>

    /**
     * GET api/v1/users/organizer-requests/pending
     * Lista richieste organizzatore in sospeso
     * 
     * Responses:
     *  - 200: OK
     *
     * @return [Call]<[kotlin.collections.List<PendingOrganizerRequestResponseDTO>]>
     */
    @GET("api/v1/users/organizer-requests/pending")
    suspend fun getPendingOrganizerRequests(): Call<kotlin.collections.List<PendingOrganizerRequestResponseDTO>>

    /**
     * GET api/v1/users/me
     * Il mio profilo
     * Recupera i dati dell&#39;utente loggato
     * Responses:
     *  - 200: OK
     *
     * @return [Call]<[UserProfileResponseDTO]>
     */
    @GET("api/v1/users/me")
    suspend fun getProfile(): Call<UserProfileResponseDTO>

    /**
     * POST api/v1/users/request-organizer
     * Richiedi ruolo organizzatore
     * 
     * Responses:
     *  - 200: OK
     *
     * @return [Call]<[Unit]>
     */
    @POST("api/v1/users/request-organizer")
    suspend fun requestOrganizerRole(): Call<Unit>

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
     * @return [Call]<[Unit]>
     */
    @PATCH("api/v1/users/admin/review-organizer/{targetUserId}")
    suspend fun reviewOrganizerRequest(@Path("targetUserId") targetUserId: kotlin.String, @Query("approved") approved: kotlin.Boolean, @Query("reason") reason: kotlin.String? = null): Call<Unit>

    /**
     * GET api/v1/users/search
     * Cerca utente
     * Ricerca pubblica di un profilo
     * Responses:
     *  - 200: OK
     *
     * @param username 
     * @return [Call]<[UserProfileResponseDTO]>
     */
    @GET("api/v1/users/search")
    suspend fun searchUser(@Query("username") username: kotlin.String): Call<UserProfileResponseDTO>

    /**
     * POST api/v1/users/sync
     * Sincronizza Utente
     * Crea o aggiorna il profilo dell&#39;utente al login
     * Responses:
     *  - 200: OK
     *
     * @param userSyncRequestDTO 
     * @return [Call]<[UserProfileResponseDTO]>
     */
    @POST("api/v1/users/sync")
    suspend fun syncUser(@Body userSyncRequestDTO: UserSyncRequestDTO): Call<UserProfileResponseDTO>

    /**
     * PUT api/v1/users/update
     * Aggiorna profilo
     * Modifica i dati del proprio profilo
     * Responses:
     *  - 200: OK
     *
     * @param userUpdateRequestDTO 
     * @return [Call]<[UserProfileResponseDTO]>
     */
    @PUT("api/v1/users/update")
    suspend fun updateProfile(@Body userUpdateRequestDTO: UserUpdateRequestDTO): Call<UserProfileResponseDTO>

}
