package it.roadies.android_app.client.apis.booking

import retrofit2.http.*
import retrofit2.Response

import okhttp3.MultipartBody

interface GestioneDocumentiApi {
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
