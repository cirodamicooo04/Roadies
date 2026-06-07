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
        val originalRequest = chain.request()
        val path = originalRequest.url.encodedPath

        if (path.contains("/public/")){
            return chain.proceed(originalRequest)
        }

        val accessToken = runBlocking {
            authRepository.getFreshAccessToken(authorizationService)
        }

        val request = if (!accessToken.isNullOrBlank()) {
            originalRequest.newBuilder()
                .addHeader("Authorization", "Bearer $accessToken")
                .build()
        } else {
            originalRequest
        }
        return chain.proceed(request)
    }
}
