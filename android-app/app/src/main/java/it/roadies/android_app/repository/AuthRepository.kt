package it.roadies.android_app.repository

import android.util.Base64
import it.roadies.android_app.auth.TokenStorage
import org.json.JSONObject
import javax.inject.Inject

class AuthRepository @Inject constructor(private val tokenStorage: TokenStorage) {
    suspend fun saveTokens( accessToken: String, refreshToken: String) {
        tokenStorage.saveTokens(accessToken, refreshToken)
    }

    suspend fun getAccessToken(): String? {
        return tokenStorage.getAccessToken()
    }

    suspend fun getRefreshToken(): String? {
        return tokenStorage.getRefreshToken()
    }

    suspend fun logout() {
        tokenStorage.clearTokens()
    }

    fun extractRoles(accessToken: String): List<String> {
        return runCatching {
            val payload = accessToken.split(".").getOrNull(1) ?: return emptyList()
            val decodedPayload = String(
                Base64.decode(payload, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING),
                Charsets.UTF_8
            )
            val jsonPayload = JSONObject(decodedPayload)
            val roles = linkedSetOf<String>()

            jsonPayload.optJSONObject("realm_access")
                ?.optJSONArray("roles")
                ?.let { rolesArray ->
                    for (index in 0 until rolesArray.length()) {
                        roles.add(rolesArray.getString(index))
                    }
                }

            jsonPayload.optJSONObject("resource_access")?.let { resourceAccess ->
                val clientIds = resourceAccess.keys()
                while (clientIds.hasNext()) {
                    val clientId = clientIds.next()
                    val clientRoles = resourceAccess
                        .optJSONObject(clientId)
                        ?.optJSONArray("roles")

                    if (clientRoles != null) {
                        for (index in 0 until clientRoles.length()) {
                            roles.add(clientRoles.getString(index))
                        }
                    }
                }
            }

            roles.toList()
        }.getOrDefault(emptyList())
    }
}
