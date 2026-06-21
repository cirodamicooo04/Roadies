package it.roadies.android_app.client.apis.user

import it.roadies.android_app.client.infrastructure.CollectionFormats
import retrofit2.http.*
import retrofit2.Call
import okhttp3.RequestBody
import com.google.gson.annotations.SerializedName

import it.roadies.android_app.client.models.user.UserDocumentRequestDTO
import it.roadies.android_app.client.models.user.UserDocumentResponseDTO

import okhttp3.MultipartBody

interface DocumentManagementApi {
    /**
     * DELETE api/v1/user-documents/{docId}
     * Elimina documento
     * Elimina un documento. Solo il proprietario o l&#39;admin possono farlo.
     * Responses:
     *  - 200: OK
     *
     * @param docId 
     * @return [Call]<[Unit]>
     */
    @DELETE("api/v1/user-documents/{docId}")
    suspend fun deleteDocument(@Path("docId") docId: java.util.UUID): Call<Unit>

    /**
     * GET api/v1/user-documents/admin/document/{userId}
     * Lista documenti utente
     * Recupera i documenti di un utente. Accessibile al proprietario, all&#39;organizzatore o all&#39;admin.
     * Responses:
     *  - 200: OK
     *
     * @param userId 
     * @return [Call]<[kotlin.collections.List<UserDocumentResponseDTO>]>
     */
    @GET("api/v1/user-documents/admin/document/{userId}")
    suspend fun getDocumentsByUser(@Path("userId") userId: kotlin.String): Call<kotlin.collections.List<UserDocumentResponseDTO>>

    /**
     * GET api/v1/user-documents/user/{userId}
     * Lista documenti utente
     * Recupera i documenti di un utente. Accessibile al proprietario, all&#39;organizzatore o all&#39;admin.
     * Responses:
     *  - 200: OK
     *
     * @param userId 
     * @return [Call]<[kotlin.collections.List<UserDocumentResponseDTO>]>
     */
    @GET("api/v1/user-documents/user/{userId}")
    suspend fun getMyDocumentsByUser(@Path("userId") userId: kotlin.String): Call<kotlin.collections.List<UserDocumentResponseDTO>>

    /**
     * POST api/v1/user-documents/upload/{userId}
     * Carica documento
     * Carica un documento. Solo l&#39;utente stesso può farlo.
     * Responses:
     *  - 200: OK
     *
     * @param userId 
     * @param document 
     * @param file 
     * @return [Call]<[UserDocumentResponseDTO]>
     */
    @Multipart
    @POST("api/v1/user-documents/upload/{userId}")
    suspend fun upload(@Path("userId") userId: kotlin.String, @Part("document") document: UserDocumentRequestDTO, @Part file: MultipartBody.Part): Call<UserDocumentResponseDTO>

    /**
     * PATCH api/v1/user-documents/verify/{docId}
     * Verifica documento
     * Approvazione o rifiuto. Solo per Organizzatori o Admin.
     * Responses:
     *  - 200: OK
     *
     * @param docId 
     * @param approved 
     * @param reason  (optional)
     * @return [Call]<[UserDocumentResponseDTO]>
     */
    @PATCH("api/v1/user-documents/verify/{docId}")
    suspend fun verifyDocument(@Path("docId") docId: java.util.UUID, @Query("approved") approved: kotlin.Boolean, @Query("reason") reason: kotlin.String? = null): Call<UserDocumentResponseDTO>

}
