package it.roadies.android_app.auth

object AuthConfig {
    const val CLIENT_ID = "roadies-android"
    const val REDIRECT_URI = "roadies://callback"
    const val AUTH_ENDPOINT = "http://10.0.2.2:8081/realms/roadies-app/protocol/openid-connect/auth"
    const val TOKEN_ENDPOINT = "http://10.0.2.2:8081/realms/roadies-app/protocol/openid-connect/token"
}
