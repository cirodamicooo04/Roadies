package it.roadies.android_app.client.apis.travel

import retrofit2.http.*
import retrofit2.Response
import com.google.gson.annotations.SerializedName

import it.roadies.android_app.client.models.travel.ActivityCreateRequest
import it.roadies.android_app.client.models.travel.ActivitySummaryResponse
import it.roadies.android_app.client.models.travel.ActivityUpdateRequest
import it.roadies.android_app.client.models.travel.ImageResponse
import it.roadies.android_app.client.models.travel.OrganizerTravelsActivityResponse
import it.roadies.android_app.client.models.travel.PageResponse
import it.roadies.android_app.client.models.travel.TravelCreateRequest
import it.roadies.android_app.client.models.travel.TravelDepartureCreateRequest
import it.roadies.android_app.client.models.travel.TravelDepartureResponse
import it.roadies.android_app.client.models.travel.TravelDepartureUpdateRequest
import it.roadies.android_app.client.models.travel.TravelResponse
import it.roadies.android_app.client.models.travel.TravelSummaryResponse
import it.roadies.android_app.client.models.travel.TravelUpdateRequest

import okhttp3.MultipartBody
import java.math.BigDecimal
import java.util.UUID

interface ViaggiApi {
    /**
     * POST api/v1/travels/{travelId}/activities
     * Aggiungi attività al viaggio
     * Aggiunge un&#39;attività a un viaggio dell&#39;organizzatore autenticato.
     * Responses:
     *  - 200: OK
     *
     * @param travelId 
     * @param activityCreateRequest 
     * @return [TravelResponse]
     */
    @POST("api/v1/travels/{travelId}/activities")
    suspend fun addActivity(@Path("travelId") travelId: UUID, @Body activityCreateRequest: ActivityCreateRequest): Response<TravelResponse>

    /**
     * POST api/v1/travels/{travelId}/departures
     * Aggiungi partenza viaggio
     * Aggiunge una nuova partenza a un viaggio dell&#39;organizzatore autenticato.
     * Responses:
     *  - 200: OK
     *
     * @param travelId 
     * @param travelDepartureCreateRequest 
     * @return [TravelDepartureResponse]
     */
    @POST("api/v1/travels/{travelId}/departures")
    suspend fun addDeparture(@Path("travelId") travelId: UUID, @Body travelDepartureCreateRequest: TravelDepartureCreateRequest): Response<TravelDepartureResponse>

    /**
     * PATCH api/v1/travels/{travelId}/departures/{departureId}/confirm
     * Conferma partenza viaggio
     * Conferma una partenza di un viaggio dell&#39;organizzatore autenticato.
     * Responses:
     *  - 200: OK
     *
     * @param travelId 
     * @param departureId 
     * @return [TravelDepartureResponse]
     */
    @PATCH("api/v1/travels/{travelId}/departures/{departureId}/confirm")
    suspend fun confirmDeparture(@Path("travelId") travelId: UUID, @Path("departureId") departureId: UUID): Response<TravelDepartureResponse>

    /**
     * POST api/v1/travels
     * Crea viaggio
     * Crea un nuovo viaggio associato all&#39;organizzatore autenticato.
     * Responses:
     *  - 200: OK
     *
     * @param travelCreateRequest 
     * @return [TravelResponse]
     */
    @POST("api/v1/travels")
    suspend fun createTravel(@Body travelCreateRequest: TravelCreateRequest): Response<TravelResponse>

    /**
     * DELETE api/v1/travels/{travelId}/activities/{activityId}
     * Rimuovi attività dal viaggio
     * Rimuove un&#39;attività collegata a un viaggio dell&#39;organizzatore autenticato.
     * Responses:
     *  - 200: OK
     *
     * @param travelId 
     * @param activityId 
     * @return [Any]
     */
    @DELETE("api/v1/travels/{travelId}/activities/{activityId}")
    suspend fun deleteActivity(@Path("travelId") travelId: UUID, @Path("activityId") activityId: UUID): Response<Any>

    /**
     * DELETE api/v1/travels/{travelId}/departures/{departureId}
     * Elimina partenza viaggio
     * Elimina una partenza da un viaggio dell&#39;organizzatore autenticato.
     * Responses:
     *  - 200: OK
     *
     * @param travelId 
     * @param departureId 
     * @return [Any]
     */
    @DELETE("api/v1/travels/{travelId}/departures/{departureId}")
    suspend fun deleteDeparture(@Path("travelId") travelId: UUID, @Path("departureId") departureId: UUID): Response<Any>

    /**
     * DELETE api/v1/travels/{id}
     * Elimina viaggio
     * Elimina un viaggio creato dall&#39;organizzatore autenticato.
     * Responses:
     *  - 200: OK
     *
     * @param id 
     * @return [Any]
     */
    @DELETE("api/v1/travels/{id}")
    suspend fun deleteTravel(@Path("id") id: UUID): Response<Any>

    /**
     * GET api/v1/travels/public/{travelId}/departures
     * Lista partenze viaggio
     * Recupera le partenze disponibili per un viaggio.
     * Responses:
     *  - 200: OK
     *
     * @param travelId 
     * @return [kotlin.collections.List<TravelDepartureResponse>]
     */
    @GET("api/v1/travels/public/{travelId}/departures")
    suspend fun getDepartures(@Path("travelId") travelId: UUID): Response<List<TravelDepartureResponse>>

    /**
     * GET api/v1/travels/my-travels
     * I miei viaggi
     * Recupera viaggi e attività creati dall&#39;organizzatore autenticato.
     * Responses:
     *  - 200: OK
     *
     * @return [OrganizerTravelsActivityResponse]
     */
    @GET("api/v1/travels/my-travels")
    suspend fun getMyTravels(): Response<OrganizerTravelsActivityResponse>

    /**
     * GET api/v1/travels/recommendations
     * Viaggi consigliati
     * Recupera i viaggi consigliati per il viaggiatore autenticato.
     * Responses:
     *  - 200: OK
     *
     * @return [kotlin.collections.List<TravelSummaryResponse>]
     */
    @GET("api/v1/travels/recommendations")
    suspend fun getRecommendations(): Response<List<TravelSummaryResponse>>

    @GET("api/v1/travels/public/recommendations")
    suspend fun getPublicRecommendations(): Response<List<TravelSummaryResponse>>

    /**
     * GET api/v1/travels/public/{id}
     * Dettaglio viaggio
     * Recupera i dettagli pubblici di un viaggio.
     * Responses:
     *  - 200: OK
     *
     * @param id 
     * @return [TravelResponse]
     */
    @GET("api/v1/travels/public/{id}")
    suspend fun getTravelById(@Path("id") id: UUID): Response<TravelResponse>

    /**
     * GET api/v1/travels/{travelId}/price
     * Prezzo viaggio
     * Recupera il prezzo del viaggio per il processo di prenotazione.
     * Responses:
     *  - 200: OK
     *
     * @param travelId 
     * @return [java.math.BigDecimal]
     */
    @GET("api/v1/travels/{travelId}/price")
    suspend fun getTravelPrice(@Path("travelId") travelId: UUID): Response<BigDecimal>

    /**
     * GET api/v1/travels/review/{travelId}
     * 
     * 
     * Responses:
     *  - 200: OK
     *
     * @param travelId 
     * @return [Unit]
     */
    @GET("api/v1/travels/review/{travelId}")
    suspend fun isValidTravel(@Path("travelId") travelId: UUID): Response<Unit>

    /**
     * GET api/v1/travels/{travelId}
     * Verifica viaggio
     * Verifica se un viaggio è valido per il processo di prenotazione.
     * Responses:
     *  - 200: OK
     *
     * @param travelId 
     * @return [Unit]
     */
    @GET("api/v1/travels/{travelId}")
    suspend fun isValidTravelDeparture(@Path("travelId") travelId: UUID): Response<Unit>


    /**
    * enum for parameter continent
    */
    enum class ContinentSearchTravels(val value: String) {
        @SerializedName(value = "EUROPE") EUROPE("EUROPE"),
        @SerializedName(value = "ASIA") ASIA("ASIA"),
        @SerializedName(value = "AMERICA") AMERICA("AMERICA"),
        @SerializedName(value = "OCEANIA") OCEANIA("OCEANIA"),
        @SerializedName(value = "AFRICA") AFRICA("AFRICA")
    }

    /**
     * GET api/v1/travels/public/search
     * Cerca viaggi o attività
     * Ricerca viaggi o attività usando filtri pubblici come destinazione, prezzo, durata, continente e paese.
     * Responses:
     *  - 200: OK
     *
     * @param page  (optional)
     * @param size  (optional)
     * @param sort  (optional)
     * @param destination  (optional)
     * @param minPrice  (optional)
     * @param maxPrice  (optional)
     * @param minDurationDays  (optional)
     * @param maxDurationDays  (optional)
     * @param type  (optional, default to "TRAVEL")
     * @param continent  (optional)
     * @param country  (optional)
     * @return [Any]
     */
    @GET("api/v1/travels/public/search")
    suspend fun searchTravels(@Query("page") page: Int? = null, @Query("size") size: Int? = null, @Query("sort") sort: List<String>? = null, @Query("destination") destination: String? = null, @Query("minPrice") minPrice: BigDecimal? = null, @Query("maxPrice") maxPrice: BigDecimal? = null, @Query("minDurationDays") minDurationDays: Int? = null, @Query("maxDurationDays") maxDurationDays: Int? = null, @Query("type") type: String? = "TRAVEL", @Query("continent") continent: ContinentSearchTravels? = null, @Query("country") country: String? = null): Response<Any>

    @GET("api/v1/travels/public/search")
    suspend fun searchTravelsTyped(
        @Query("destination") destination: String? = null,
        @Query("minPrice") minPrice: BigDecimal? = null,
        @Query("maxPrice") maxPrice: BigDecimal? = null,
        @Query("minDurationDays") minDurationDays: Int? = null,
        @Query("maxDurationDays") maxDurationDays: Int? = null,
        @Query("continent") continent: ContinentSearchTravels? = null,
        @Query("country") country: String? = null,
        @Query("page") page: Int? = null,
        @Query("size") size: Int? = null,
        @Query("sort") sort: List<String>? = null,
        @Query("type") type: String = "TRAVEL"
    ): Response<PageResponse<TravelSummaryResponse>>

    @GET("api/v1/travels/public/search")
    suspend fun searchActivitiesTyped(
        @Query("destination") destination: String? = null,
        @Query("minPrice") minPrice: BigDecimal? = null,
        @Query("maxPrice") maxPrice: BigDecimal? = null,
        @Query("minDurationDays") minDurationDays: Int? = null,
        @Query("maxDurationDays") maxDurationDays: Int? = null,
        @Query("continent") continent: ContinentSearchTravels? = null,
        @Query("country") country: String? = null,
        @Query("page") page: Int? = null,
        @Query("size") size: Int? = null,
        @Query("sort") sort: List<String>? = null,
        @Query("type") type: String = "ACTIVITY"
    ): Response<PageResponse<ActivitySummaryResponse>>

    @GET("api/v1/travels/public/organizer/{id}")
    suspend fun getOrganizerTravelsActivity(@Path("id") id: String): Response<OrganizerTravelsActivityResponse>

    /**
     * PUT api/v1/travels/{travelId}/activities/{activityId}
     * Aggiorna attività del viaggio
     * Modifica un&#39;attività collegata a un viaggio dell&#39;organizzatore autenticato.
     * Responses:
     *  - 200: OK
     *
     * @param travelId 
     * @param activityId 
     * @param activityUpdateRequest 
     * @return [TravelResponse]
     */
    @PUT("api/v1/travels/{travelId}/activities/{activityId}")
    suspend fun updateActivity(@Path("travelId") travelId: UUID, @Path("activityId") activityId: UUID, @Body activityUpdateRequest: ActivityUpdateRequest): Response<TravelResponse>

    /**
     * PUT api/v1/travels/{travelId}/departures/{departureId}
     * Aggiorna partenza viaggio
     * Modifica una partenza di un viaggio dell&#39;organizzatore autenticato.
     * Responses:
     *  - 200: OK
     *
     * @param travelId 
     * @param departureId 
     * @param travelDepartureUpdateRequest 
     * @return [TravelDepartureResponse]
     */
    @PUT("api/v1/travels/{travelId}/departures/{departureId}")
    suspend fun updateDeparture(@Path("travelId") travelId: UUID, @Path("departureId") departureId: UUID, @Body travelDepartureUpdateRequest: TravelDepartureUpdateRequest): Response<TravelDepartureResponse>

    /**
     * PUT api/v1/travels/{id}
     * Aggiorna viaggio
     * Modifica i dati di un viaggio creato dall&#39;organizzatore autenticato.
     * Responses:
     *  - 200: OK
     *
     * @param id 
     * @param travelUpdateRequest 
     * @return [TravelResponse]
     */
    @PUT("api/v1/travels/{id}")
    suspend fun updateTravel(@Path("id") id: UUID, @Body travelUpdateRequest: TravelUpdateRequest): Response<TravelResponse>

    /**
     * POST api/v1/travels/images
     * Carica immagine
     * Carica un&#39;immagine associata all&#39;organizzatore autenticato.
     * Responses:
     *  - 200: OK
     *
     * @param file 
     * @return [ImageResponse]
     */
    @Multipart
    @POST("api/v1/travels/images")
    suspend fun uploadImage(@Part file: MultipartBody.Part): Response<ImageResponse>

}
