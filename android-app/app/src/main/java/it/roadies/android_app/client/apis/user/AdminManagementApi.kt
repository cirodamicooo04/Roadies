package it.roadies.android_app.client.apis.admin

import it.roadies.android_app.client.models.user.PendingOrganizerRequestResponseDTO
import it.roadies.android_app.client.models.user.UserResponseDTO
import retrofit2.Response
import retrofit2.http.*

interface AdminManagementApi {

    // (PUT /api/v1/admin/users/{id}/block)
    @PUT("api/v1/admin/users/{id}/block")
    suspend fun blockUser(@Path("id") keycloakId: String): Response<Void>

    // (PUT /api/v1/admin/users/{id}/unblock)
    @PUT("api/v1/admin/users/{id}/unblock")
    suspend fun unblockUser(@Path("id") keycloakId: String): Response<Void>

    // (PATCH /api/v1/admin/review-organizer/{targetUserId})
    @PATCH("api/v1/admin/review-organizer/{targetUserId}")
    suspend fun reviewOrganizerRequest(
        @Path("targetUserId") targetKeycloakId: String,
        @Query("approved") approved: Boolean,
        @Query("reason") reason: String?
    ): Response<Void>

    // (GET /api/v1/admin/organizer-requests/pending)
    @GET("api/v1/admin/organizer-requests/pending")
    suspend fun getPendingOrganizerRequests(): Response<List<PendingOrganizerRequestResponseDTO>>

    // (GET /api/v1/admin/users)
    @GET("api/v1/admin/users")
    suspend fun getUsersByFilter(@Query("filter") filter: String): Response<List<UserResponseDTO>>
}