package it.roadies.android_app.client.apis.user

import it.roadies.android_app.client.infrastructure.CollectionFormats.*
import retrofit2.http.*
import retrofit2.Call
import okhttp3.RequestBody
import com.google.gson.annotations.SerializedName

import it.roadies.android_app.client.models.user.FriendshipResponseDTO
import it.roadies.android_app.client.models.user.UserProfileResponseDTO
import retrofit2.Response

interface FriendshipManagementApi {
    /**
     * GET api/v1/friends/detailed-list
     * Lista amici dettagliata
     * Restituisce i profili degli amici con dettagli sulla data di inizio amicizia.
     * Responses:
     *  - 200: OK
     *
     * @return [Response]<[kotlin.collections.List<FriendshipResponseDTO>]>
     */
    @GET("api/v1/friends/detailed-list")
    suspend fun getDetailedFriends(): Response<kotlin.collections.List<FriendshipResponseDTO>>

    /**
     * GET api/v1/friends/list
     * Lista amici rapida
     * Restituisce i profili base di tutti gli amici confermati.
     * Responses:
     *  - 200: OK
     *
     * @return [Response]<[kotlin.collections.List<UserProfileResponseDTO>]>
     */
    @GET("api/v1/friends/list")
    suspend fun getFriends(): Response<kotlin.collections.List<UserProfileResponseDTO>>

    /**
     * GET api/v1/friends/requests/pending
     * Richieste in sospeso
     * Recupera le richieste di amicizia ricevute in attesa di risposta.
     * Responses:
     *  - 200: OK
     *
     * @return [Response]<[kotlin.collections.List<FriendshipResponseDTO>]>
     */
    @GET("api/v1/friends/requests/pending")
    suspend fun getPendingRequests(): Response<kotlin.collections.List<FriendshipResponseDTO>>

    /**
     * DELETE api/v1/friends/{friendshipId}
     * Rimuovi un amico
     * Elimina una relazione di amicizia esistente.
     * Responses:
     *  - 204: Amicizia rimossa correttamente
     *
     * @param friendshipId 
     * @return [Response]<[Unit]>
     */
    @DELETE("api/v1/friends/{friendUsername}")
    suspend fun removeFriend(@Path("friendUsername") friendUsername: String): Response<Unit>


    /**
    * enum for parameter status
    */
    enum class StatusRespond(val value: kotlin.String) {
        @SerializedName(value = "PENDING") PENDING("PENDING"),
        @SerializedName(value = "ACCEPTED") ACCEPTED("ACCEPTED"),
        @SerializedName(value = "REJECTED") REJECTED("REJECTED")
    }

    /**
     * PATCH api/v1/friends/respond/{friendshipId}
     * Rispondi a una richiesta
     * Accetta o rifiuta una richiesta di amicizia ricevuta.
     * Responses:
     *  - 200: OK
     *
     * @param friendshipId 
     * @param status 
     * @return [Response]<[Unit]>
     */
    @PATCH("api/v1/friends/respond/{friendshipId}")
    suspend fun respond(@Path("friendshipId") friendshipId: java.util.UUID, @Query("status") status: StatusRespond): Response<Unit>

    /**
     * POST api/v1/friends/request/{receiverUsername}
     * Invia una richiesta
     * Invia una richiesta di amicizia a un utente tramite il suo username.
     * Responses:
     *  - 200: Richiesta inviata con successo
     *
     * @param receiverUsername 
     * @return [Response]<[Unit]>
     */
    @POST("api/v1/friends/request/{receiverUsername}")
    suspend fun send(@Path("receiverUsername") receiverUsername: kotlin.String): Response<Unit>

    @GET("api/v1/friends/requests/sent")
    suspend fun getSentRequests(): Response<List<FriendshipResponseDTO>>
}
