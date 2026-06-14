package it.roadies.android_app.auth

import android.net.Uri
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationServiceConfiguration
import net.openid.appauth.ResponseTypeValues

class AppAuthManager {

    private val serviceConfig = AuthorizationServiceConfiguration(
        Uri.parse(AuthConfig.AUTH_ENDPOINT),
        Uri.parse(AuthConfig.TOKEN_ENDPOINT)
    )

    fun buildLoginRequest(): AuthorizationRequest {
        return AuthorizationRequest.Builder(
            serviceConfig,
            AuthConfig.CLIENT_ID,
            ResponseTypeValues.CODE,
            Uri.parse(AuthConfig.REDIRECT_URI)
        )
            .setScope("openid profile email")
            .build()
    }
}