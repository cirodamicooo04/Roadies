package it.roadies.android_app.client.models.user

import com.google.gson.annotations.SerializedName

class MinimalInformationResponseDTO {

    @SerializedName("keycloakId")
    val keycloakId: String? = null

    @SerializedName("username")
    val username: String? = null

    @SerializedName("avatarUrl")
    val avatarUrl: String? = null
}