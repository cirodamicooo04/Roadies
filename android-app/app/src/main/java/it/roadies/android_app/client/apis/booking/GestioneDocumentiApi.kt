package it.roadies.android_app.client.apis.booking

import it.roadies.android_app.client.infrastructure.CollectionFormats.*
import retrofit2.http.*
import retrofit2.Response
import okhttp3.RequestBody
import com.google.gson.annotations.SerializedName

import it.roadies.android_app.client.models.booking.MemberDocumentUpdateRequest

import okhttp3.MultipartBody

interface GestioneDocumentiApi {
    /**
     * PATCH api/v1/booking-documents/accept
     * Conferma la validazione di un documento
     * Permette di validare un documento relativo ad una prenotazione
     * Responses:
     *  - 200: Documento accettato con successo
     *  - 401: Utente non autenticato
     *  - 403: Utente non autorizzato
     *
     * @param memberDocumentUpdateRequest 
     * @return [Unit]
     */
    @PATCH("api/v1/booking-documents/accept")
    suspend fun acceptDocument(@Body memberDocumentUpdateRequest: MemberDocumentUpdateRequest): Response<Unit>

    /**
     * PATCH api/v1/booking-documents/reject
     * Rifiuta la validazione di un documento
     * Permette di rifiutare un documento relativo ad una prenotazione
     * Responses:
     *  - 200: Documento rifiutato con successo
     *  - 401: Utente non autenticato
     *  - 403: Utente non autorizzato
     *
     * @param memberDocumentUpdateRequest 
     * @return [Unit]
     */
    @PATCH("api/v1/booking-documents/reject")
    suspend fun rejectDocument(@Body memberDocumentUpdateRequest: MemberDocumentUpdateRequest): Response<Unit>

    /**
     * POST api/v1/booking-documents/{documentId}/upload
     * Invia documenti
     * Permette di inserire un documento relativo ad una prenotazione
     * Responses:
     *  - 200: Documento inserito con successo
     *  - 401: Utente non autenticato
     *  - 403: Utente non autorizzato
     *
     * @param documentId 
     * @param file 
     * @return [kotlin.String]
     */
    @Multipart
    @POST("api/v1/booking-documents/{documentId}/upload")
    suspend fun uploadDocumentPhoto(@Path("documentId") documentId: java.util.UUID, @Part file: MultipartBody.Part): Response<kotlin.String>

}
