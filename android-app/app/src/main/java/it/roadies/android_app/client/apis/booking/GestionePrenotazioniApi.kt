package it.roadies.android_app.client.apis

import it.roadies.android_app.client.infrastructure.CollectionFormats.*
import retrofit2.http.*
import retrofit2.Response
import okhttp3.RequestBody
import com.google.gson.annotations.SerializedName

import it.roadies.android_app.client.models.BookingCreateRequest
import it.roadies.android_app.client.models.BookingDraftRequest
import it.roadies.android_app.client.models.BookingDraftResponse
import it.roadies.android_app.client.models.BookingMemberRequest
import it.roadies.android_app.client.models.BookingStatusResponse
import it.roadies.android_app.client.models.BookingStep2Response

interface GestionePrenotazioniApi {
    /**
     * POST api/v1/bookings/draft
     * Crea una bozza di prenotazione (passo 1)
     * Inizializza una nuova prenotazione in stato DRAFT
     * Responses:
     *  - 201: Bozza creata con successo
     *  - 400: Dati della richiesta non validi
     *  - 401: Utente non autenticato
     *  - 403: Utente non autorizzato
     *
     * @param bookingDraftRequest 
     * @return [BookingDraftResponse]
     */
    @POST("api/v1/bookings/draft")
    suspend fun createDraft(@Body bookingDraftRequest: BookingDraftRequest): Response<BookingDraftResponse>

    /**
     * PUT api/v1/bookings/pending
     * Richiedi riserva posti per un tempo pre-stabilito (passo 2)
     * Permette di riservare i posti, se ancora disponibili, e iniziare il processo di prenotazione dopo la creazione della draft
     * Responses:
     *  - 200: Booking aggiornato
     *  - 400: Dati della richiesta non validi
     *  - 401: Utente non autenticato
     *  - 403: Utente non autorizzato
     *
     * @param bookingCreateRequest 
     * @return [Unit]
     */
    @PUT("api/v1/bookings/pending")
    suspend fun createPendingAndReserveSeats(@Body bookingCreateRequest: BookingCreateRequest): Response<Unit>

    /**
     * DELETE api/v1/bookings/{bookingId}
     * Elimina una prenotazione
     * Permette l&#39;eleminazione una prenotazione precedenetemente creata
     * Responses:
     *  - 200: Prenotazione eliminata con successo
     *  - 401: Utente non autenticato
     *  - 403: Utente non autorizzato
     *
     * @param bookingId 
     * @return [Unit]
     */
    @DELETE("api/v1/bookings/{bookingId}")
    suspend fun deleteBooking(@Path("bookingId") bookingId: java.util.UUID): Response<Unit>

    /**
     * GET api/v1/bookings/users/me
     * Ottieni tutte le prenotazioni
     * Permette di ottenere tutte le prenotazioni dell&#39;utente che ne fa richiesta
     * Responses:
     *  - 200: Prenotazioni restituite con successo
     *  - 401: Utente non autenticato
     *  - 403: Utente non autorizzato
     *
     * @return [kotlin.collections.List<java.util.UUID>]
     */
    @GET("api/v1/bookings/users/me")
    suspend fun getBookingsFromUser(): Response<kotlin.collections.List<java.util.UUID>>

    /**
     * GET api/v1/bookings/{bookingId}/status
     * Ottieni lo stato della prenotazione
     * Restituisce lo stato attuale di una prenotazione specifica tramite il suo ID.
     * Responses:
     *  - 200: Stato recuperato con successo
     *  - 404: Prenotazione non trovata
     *  - 401: Utente non autenticato
     *  - 403: Utente non autorizzato
     *
     * @param bookingId 
     * @return [BookingStatusResponse]
     */
    @GET("api/v1/bookings/{bookingId}/status")
    suspend fun getStatus(@Path("bookingId") bookingId: java.util.UUID): Response<BookingStatusResponse>

    /**
     * POST api/v1/bookings/members
     * Inizializza membri (passo 3)
     * Permette di inizializzare la lista dei membri di una prenotazione in stato PENDING
     * Responses:
     *  - 200: Booking aggiornato
     *  - 400: Dati della richiesta non validi
     *  - 401: Utente non autenticato
     *  - 403: Utente non autorizzato
     *  - 404: Prenotazione non trovata
     *
     * @param bookingMemberRequest 
     * @return [BookingStep2Response]
     */
    @POST("api/v1/bookings/members")
    suspend fun insertMembers(@Body bookingMemberRequest: BookingMemberRequest): Response<BookingStep2Response>

}
