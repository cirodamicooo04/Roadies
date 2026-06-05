package it.roadies.android_app.client.network

import it.roadies.android_app.repository.AuthRepository
import kotlinx.coroutines.runBlocking
import net.openid.appauth.AuthorizationService
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthTokenInterceptor @Inject constructor(
    private val authRepository: AuthRepository,
    private val authorizationService: AuthorizationService
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val accessToken = runBlocking {
            authRepository.getFreshAccessToken(authorizationService)
        }
        val request = if (!accessToken.isNullOrBlank()) {
            chain.request()
                .newBuilder()
                .addHeader("Authorization", "Bearer $accessToken")
                .build()
        } else {
            chain.request()
        }
        return chain.proceed(request)
    }
}
