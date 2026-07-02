package it.roadies.android_app.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.roadies.android_app.client.models.user.UserSyncRequestDTO
import it.roadies.android_app.repository.AuthRepository
import it.roadies.android_app.repository.UserRepository
import kotlinx.coroutines.launch
import net.openid.appauth.AuthState as AppAuthState
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    val authState = authRepository.authState

    init {
        checkSession()
    }

    private fun checkSession() {
        viewModelScope.launch {
            authRepository.checkSession()
        }
    }

    fun onLoginSuccess(appAuthState: AppAuthState) {
        viewModelScope.launch {
            authRepository.saveAuthState(appAuthState)
            Log.d(TAG, "Login completato: stato AppAuth salvato")

            val accessToken = appAuthState.accessToken
            if (accessToken.isNullOrBlank()) {
                Log.e(TAG, "Access token nullo dopo login")
                return@launch
            }

            val claims = authRepository.getUserClaims(accessToken)
            if (claims == null) {
                Log.e(TAG, "Impossibile leggere i claims dal token")
                return@launch
            }

            val syncResponse = userRepository.syncUser(
                UserSyncRequestDTO(
                    email = claims.email,
                    username = claims.preferredUsername,
                    firstName = claims.firstName,
                    lastName = claims.lastName,
                    keycloakId = claims.sub
                )
            )

            if (syncResponse.success) {
                Log.d(TAG, "syncUser completata con successo")
            } else {
                Log.e(TAG, "syncUser fallita: ${syncResponse.errorMessage}")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            userRepository.clearLocalUser()
            authRepository.logout()
            Log.d(TAG, "Logout completato: token e cache locale rimossi")
        }
    }

    companion object {
        private const val TAG = "AuthViewModel"
    }
}