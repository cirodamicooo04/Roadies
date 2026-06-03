package it.roadies.android_app.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.roadies.android_app.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import net.openid.appauth.AuthState as AppAuthState
import javax.inject.Inject

data class AuthState(
    val isLoading: Boolean = true,
    val isLogged: Boolean = false,
    val roles: List<String> = emptyList()
)

@HiltViewModel
class AuthViewModel @Inject constructor(private val authRepository: AuthRepository) : ViewModel() {

    private val _authState = MutableStateFlow(AuthState())
    val authState = _authState.asStateFlow()

    init {
        checkSession()
    }

    private fun checkSession() {
        viewModelScope.launch {
            val appAuthState = authRepository.getAuthState()
            val accessToken = appAuthState.accessToken

            if (!appAuthState.isAuthorized || accessToken == null) {
                Log.d(TAG, "Sessione non trovata: utente non loggato")
                _authState.value = AuthState(
                    isLoading = false,
                    isLogged = false
                )
            } else {
                Log.d(TAG, "Sessione trovata: utente loggato")
                _authState.value = AuthState(
                    isLoading = false,
                    isLogged = true,
                    roles = authRepository.extractRoles(accessToken)
                )
            }
        }
    }

    fun onLoginSuccess(appAuthState: AppAuthState) {
        viewModelScope.launch {
            authRepository.saveAuthState(appAuthState)
            Log.d(TAG, "Login completato: stato AppAuth salvato")

            val accessToken = appAuthState.accessToken

            _authState.value = AuthState(
                isLoading = false,
                isLogged = true,
                roles = accessToken?.let { authRepository.extractRoles(it) }.orEmpty()
            )
        }
    }

    fun logout() {
        viewModelScope.launch {
            //Fare anche logout da remoto
            authRepository.logout()
            Log.d(TAG, "Logout completato: token rimossi")
            _authState.value = AuthState(
                isLoading = false,
                isLogged = false,
                roles = emptyList()
            )
        }
    }

    companion object {
        private const val TAG = "AuthViewModel"
    }
}
