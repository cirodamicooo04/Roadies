package it.roadies.android_app.client.apis.travel

import it.roadies.android_app.client.models.travel.PhotonResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface PhotonApi {
    @GET("api/")
    suspend fun getSuggestions(
        @Query("q") query: String,
        @Query("limit") limit: Int = 10,
        @Query("lang") lang: String = "en"
    ) : Response<PhotonResponse>

}