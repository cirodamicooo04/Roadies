package it.roadies.android_app.client.apis.travel

import retrofit2.http.*
import retrofit2.Response

import it.roadies.android_app.client.models.travel.TagResponse

interface MetadatiApi {
    /**
     * POST api/v1/metadata/tags
     * Aggiungi tag
     * Crea un nuovo tag. Operazione riservata agli amministratori.
     * Responses:
     *  - 200: OK
     *
     * @param body 
     * @return [TagResponse]
     */
    @POST("api/v1/metadata/tags")
    suspend fun addTag(@Body body: String): Response<TagResponse>

    /**
     * GET api/v1/metadata/public/continents
     * Lista continenti
     * Recupera l&#39;elenco dei continenti disponibili.
     * Responses:
     *  - 200: OK
     *
     * @return [kotlin.collections.List<kotlin.String>]
     */
    @GET("api/v1/metadata/public/continents")
    suspend fun getContinents(): Response<List<String>>

    /**
     * GET api/v1/metadata/public/tags
     * Lista tag
     * Recupera tutti i tag disponibili per classificare viaggi e attività.
     * Responses:
     *  - 200: OK
     *
     * @return [kotlin.collections.List<TagResponse>]
     */
    @GET("api/v1/metadata/public/tags")
    suspend fun getTags(): Response<List<TagResponse>>

}
