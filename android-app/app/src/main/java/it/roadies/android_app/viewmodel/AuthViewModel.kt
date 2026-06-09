package it.roadies.android_app.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.roadies.android_app.repository.AuthRepository
import kotlinx.coroutines.launch
import net.openid.appauth.AuthState as AppAuthState
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(private val authRepository: AuthRepository) : ViewModel() {

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
        }
    }

    fun logout() {
        viewModelScope.launch {
            //Fare anche logout da remoto
            authRepository.logout()
            Log.d(TAG, "Logout completato: token rimossi")
        }
    }

    companion object {
        private const val TAG = "AuthViewModel"
    }
}
