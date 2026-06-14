package it.roadies.android_app.repository

import android.util.Base64
import it.roadies.android_app.auth.TokenStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import net.openid.appauth.AuthorizationService
import net.openid.appauth.AuthState
import org.json.JSONObject
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton

data class AuthSessionState(
    val isLoading: Boolean = true,
    val isLogged: Boolean = false,
    val roles: List<String> = emptyList()
)

@Singleton
class AuthRepository @Inject constructor(private val tokenStorage: TokenStorage) {

    private val _authState = MutableStateFlow(AuthSessionState())
    val authState = _authState.asStateFlow()

    suspend fun checkSession() {
        val appAuthState = getAuthState()
        _authState.value = appAuthState.toAuthSessionState()
    }

    suspend fun saveAuthState(authState: AuthState) {
        tokenStorage.saveAuthState(authState.jsonSerializeString())
        _authState.value = authState.toAuthSessionState()
    }

    suspend fun getAuthState(): AuthState {
        return readAuthState() ?: AuthState()
    }

    suspend fun getFreshAccessToken(authorizationService: AuthorizationService): String? {
        val authState = getAuthState()
        if (!authState.isAuthorized) return null

        return suspendCancellableCoroutine { continuation ->
            authState.performActionWithFreshTokens(authorizationService) { accessToken, _, exception ->
                if (continuation.isCancelled) return@performActionWithFreshTokens

                if (exception != null) {
                    continuation.resume(null)
                    return@performActionWithFreshTokens
                }

                continuation.resume(accessToken)
            }
        }.also {
            saveAuthState(authState)
        }
    }

    suspend fun logout() {
        tokenStorage.clearAuthState()
        _authState.value = AuthSessionState(
            isLoading = false,
            isLogged = false,
            roles = emptyList()
        )
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

    private suspend fun readAuthState(): AuthState? {
        val authStateJson = tokenStorage.getAuthStateJson() ?: return null
        return runCatching {
            AuthState.jsonDeserialize(authStateJson)
        }.getOrNull()
    }

    private fun AuthState.toAuthSessionState(): AuthSessionState {
        val currentAccessToken = this.accessToken

        if (!isAuthorized || currentAccessToken == null) {
            return AuthSessionState(
                isLoading = false,
                isLogged = false,
                roles = emptyList()
            )
        }

        return AuthSessionState(
            isLoading = false,
            isLogged = true,
            roles = extractRoles(currentAccessToken)
        )
    }
}
