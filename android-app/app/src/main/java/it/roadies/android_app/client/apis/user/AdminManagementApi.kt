package it.roadies.android_app.client.apis.user

import it.roadies.android_app.client.models.user.UserProfileResponseDTO
import it.roadies.android_app.client.models.user.PendingOrganizerRequestResponseDTO
import retrofit2.http.*

interface AdminManagementApi {

    // Block user
    @PUT("/api/v1/admin/users/{keycloakId}/block")
    suspend fun blockUser(@Path("keycloakId") keycloakId: String)

    // Unblock user
    @PUT("/api/v1/admin/users/{keycloakId}/unblock")
    suspend fun unblockUser(@Path("keycloakId") keycloakId: String)

    // Demote organizer
    @PUT("/api/v1/admin/organizers/{keycloakId}/demote")
    suspend fun demoteOrganizer(@Path("keycloakId") keycloakId: String)

    // Accept/Reject organizer request
    @PUT("/api/v1/admin/organizers/{keycloakId}/review")
    suspend fun reviewOrganizerRequest(
        @Path("keycloakId") keycloakId: String,
        @Query("approved") approved: Boolean,
        @Query("reason") reason: String?
    )

    // Get organizer requests
    @GET("/api/v1/admin/organizers/pending")
    suspend fun getPendingOrganizerRequests(): List<PendingOrganizerRequestResponseDTO>
}