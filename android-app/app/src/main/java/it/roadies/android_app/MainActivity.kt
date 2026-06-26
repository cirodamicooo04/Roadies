package it.roadies.android_app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import dagger.hilt.android.AndroidEntryPoint
import it.roadies.android_app.auth.AppAuthManager
import it.roadies.android_app.ui.theme.AndroidappTheme
import it.roadies.android_app.viewmodel.AuthViewModel
import net.openid.appauth.AppAuthConfiguration
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.AuthorizationService
import net.openid.appauth.AuthState
import net.openid.appauth.connectivity.ConnectionBuilder
import java.net.HttpURLConnection
import java.net.URL

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val authViewModel: AuthViewModel by viewModels()
    private val appAuthManager: AppAuthManager = AppAuthManager()
    private lateinit var authorizationService: AuthorizationService
    private lateinit var loginLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val appAuthConfiguration = AppAuthConfiguration.Builder()
            .setConnectionBuilder(devConnectionBuilder)
            .build()

        authorizationService = AuthorizationService(this, appAuthConfiguration)

        loginLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            handleLoginResult(result.data)
        }

        setContent {
            AndroidappTheme {
                RoadiesApp(
                    authViewModel = authViewModel,
                    onLoginClick = {
                        onLogin()
                    }
                )
            }
        }
    }

    private fun onLogin(){
        try {
            val authRequest = appAuthManager.buildLoginRequest()
            val authIntent = authorizationService.getAuthorizationRequestIntent(authRequest)
            loginLauncher.launch(authIntent)
        } catch (exception: Exception) {
            Log.e(TAG, "Errore durante l'avvio del login", exception)
        }
    }

    private fun handleLoginResult(data: Intent?){
        if (data == null) return

        val authException = AuthorizationException.fromIntent(data)
        if (authException != null) {
            Log.e(TAG, "Errore nella risposta di autorizzazione", authException)
            return
        }

        val authResponse = AuthorizationResponse.fromIntent(data) ?: return
        val appAuthState = AuthState()
        appAuthState.update(authResponse, authException)

        val tokenRequest = authResponse.createTokenExchangeRequest()

        authorizationService.performTokenRequest(tokenRequest) { tokenResponse, tokenException ->
            appAuthState.update(tokenResponse, tokenException)

            if (tokenException != null) {
                Log.e(TAG, "Errore durante lo scambio token", tokenException)
                return@performTokenRequest
            }

            if (tokenResponse == null) {
                Log.e(TAG, "Risposta token vuota")
                return@performTokenRequest
            }

            authViewModel.onLoginSuccess(appAuthState)
        }
    }

    override fun onDestroy() {
        authorizationService.dispose()
        super.onDestroy()
    }

    companion object {
        private const val TAG = "MainActivity"

        private val devConnectionBuilder = ConnectionBuilder { uri ->
            URL(uri.toString()).openConnection() as HttpURLConnection
        }
    }
}
