package it.roadies.android_app.client.apis.travel

import retrofit2.http.*
import retrofit2.Response

import it.roadies.android_app.client.models.travel.ActivityCreateRequest
import it.roadies.android_app.client.models.travel.ActivityDepartureCreateRequest
import it.roadies.android_app.client.models.travel.ActivityDepartureResponse
import it.roadies.android_app.client.models.travel.ActivityDepartureUpdateRequest
import it.roadies.android_app.client.models.travel.ActivityResponse
import it.roadies.android_app.client.models.travel.ActivityUpdateRequest
import java.math.BigDecimal
import java.util.UUID

interface AttivitApi {
    /**
     * POST api/v1/activities/{activityId}/departures
     * Aggiungi partenza attività
     * Aggiunge una nuova partenza a un&#39;attività dell&#39;organizzatore autenticato.
     * Responses:
     *  - 200: OK
     *
     * @param activityId 
     * @param activityDepartureCreateRequest 
     * @return [ActivityDepartureResponse]
     */
    @POST("api/v1/activities/{activityId}/departures")
    suspend fun addDeparture1(@Path("activityId") activityId: UUID, @Body activityDepartureCreateRequest: ActivityDepartureCreateRequest): Response<ActivityDepartureResponse>

    /**
     * PATCH api/v1/activities/{activityId}/departures/{departureId}/confirm
     * Conferma partenza attività
     * Conferma una partenza di un&#39;attività.
     * Responses:
     *  - 200: OK
     *
     * @param activityId 
     * @param departureId 
     * @return [ActivityDepartureResponse]
     */
    @PATCH("api/v1/activities/{activityId}/departures/{departureId}/confirm")
    suspend fun confirmDeparture1(@Path("activityId") activityId: UUID, @Path("departureId") departureId: UUID): Response<ActivityDepartureResponse>

    /**
     * POST api/v1/activities
     * Crea attività
     * Crea una nuova attività associata all&#39;organizzatore autenticato.
     * Responses:
     *  - 200: OK
     *
     * @param activityCreateRequest 
     * @return [ActivityResponse]
     */
    @POST("api/v1/activities")
    suspend fun createActivity(@Body activityCreateRequest: ActivityCreateRequest): Response<ActivityResponse>

    /**
     * DELETE api/v1/activities/{id}
     * Elimina attività
     * Elimina un&#39;attività creata dall&#39;organizzatore autenticato.
     * Responses:
     *  - 200: OK
     *
     * @param id 
     * @return [Any]
     */
    @DELETE("api/v1/activities/{id}")
    suspend fun deleteActivity1(@Path("id") id: UUID): Response<Any>

    /**
     * DELETE api/v1/activities/{activityId}/departures/{departureId}
     * Elimina partenza attività
     * Elimina una partenza da un&#39;attività dell&#39;organizzatore autenticato.
     * Responses:
     *  - 200: OK
     *
     * @param activityId 
     * @param departureId 
     * @return [Any]
     */
    @DELETE("api/v1/activities/{activityId}/departures/{departureId}")
    suspend fun deleteDeparture1(@Path("activityId") activityId: UUID, @Path("departureId") departureId: UUID): Response<Any>

    /**
     * GET api/v1/activities/public/{id}
     * Dettaglio attività
     * Recupera i dettagli pubblici di un&#39;attività.
     * Responses:
     *  - 200: OK
     *
     * @param id 
     * @return [ActivityResponse]
     */
    @GET("api/v1/activities/public/{id}")
    suspend fun getActivity(@Path("id") id: UUID): Response<ActivityResponse>

    /**
     * GET api/v1/activities/{activityId}/price
     * Prezzo attività
     * Recupera il prezzo dell&#39;attività per il processo di prenotazione.
     * Responses:
     *  - 200: OK
     *
     * @param activityId 
     * @return [java.math.BigDecimal]
     */
    @GET("api/v1/activities/{activityId}/price")
    suspend fun getActivityPrice(@Path("activityId") activityId: UUID): Response<BigDecimal>

    /**
     * GET api/v1/activities/public/{activityId}/departures
     * Lista partenze attività
     * Recupera le partenze disponibili per un&#39;attività.
     * Responses:
     *  - 200: OK
     *
     * @param activityId 
     * @return [kotlin.collections.List<ActivityDepartureResponse>]
     */
    @GET("api/v1/activities/public/{activityId}/departures")
    suspend fun getDepartures1(@Path("activityId") activityId: UUID): Response<List<ActivityDepartureResponse>>

    /**
     * GET api/v1/activities/{activityId}
     * Verifica attività
     * Verifica se un&#39;attività è valida per il processo di prenotazione.
     * Responses:
     *  - 200: OK
     *
     * @param activityId 
     * @return [Unit]
     */
    @GET("api/v1/activities/{activityId}")
    suspend fun isValidActivity(@Path("activityId") activityId: UUID): Response<Unit>

    /**
     * GET api/v1/activities/review/{activityId}/
     * 
     * 
     * Responses:
     *  - 200: OK
     *
     * @param activityId 
     * @return [Unit]
     */
    @GET("api/v1/activities/review/{activityId}/")
    suspend fun isValidActivityAndIsNotIntoATravel(@Path("activityId") activityId: UUID): Response<Unit>

    /**
     * PUT api/v1/activities/{id}
     * Aggiorna attività
     * Modifica i dati di un&#39;attività creata dall&#39;organizzatore autenticato.
     * Responses:
     *  - 200: OK
     *
     * @param id 
     * @param activityUpdateRequest 
     * @return [ActivityResponse]
     */
    @PUT("api/v1/activities/{id}")
    suspend fun updateActivity1(@Path("id") id: UUID, @Body activityUpdateRequest: ActivityUpdateRequest): Response<ActivityResponse>

    /**
     * PUT api/v1/activities/{activityId}/departures/{departureId}
     * Aggiorna partenza attività
     * Modifica una partenza di un&#39;attività dell&#39;organizzatore autenticato.
     * Responses:
     *  - 200: OK
     *
     * @param activityId 
     * @param departureId 
     * @param activityDepartureUpdateRequest 
     * @return [ActivityDepartureResponse]
     */
    @PUT("api/v1/activities/{activityId}/departures/{departureId}")
    suspend fun updateDeparture1(@Path("activityId") activityId: UUID, @Path("departureId") departureId: UUID, @Body activityDepartureUpdateRequest: ActivityDepartureUpdateRequest): Response<ActivityDepartureResponse>

}
