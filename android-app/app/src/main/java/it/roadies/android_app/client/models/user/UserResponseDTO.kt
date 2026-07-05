package it.roadies.android_app.client.models.user

import com.google.gson.annotations.SerializedName

data class UserResponseDTO(
    @SerializedName("keycloakId") val keycloakId: String,
    @SerializedName("username") val username: String?,
    @SerializedName("firstName") val firstName: String?,
    @SerializedName("lastName") val lastName: String?,
    @SerializedName("email") val email: String?,
    @SerializedName("avatarUrl") val avatarUrl: String?,
    @SerializedName("points") val points: Long?,
    @SerializedName("badge") val badge: String?,
    @SerializedName("enabled") val enabled: Boolean?
)