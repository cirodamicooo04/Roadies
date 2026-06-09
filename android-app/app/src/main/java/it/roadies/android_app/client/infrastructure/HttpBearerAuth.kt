package it.roadies.android_app.client.infrastructure

import android.R.attr.scheme
import okhttp3.Interceptor
import okhttp3.Response

class HttpBearerAuth(private val scheme: String = "Bearer") : Interceptor {
    var bearerToken: String? = null

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = bearerToken
        val request = if (token.isNullOrBlank()) {
            chain.request()
        } else {
            chain.request()
                .newBuilder()
                .addHeader("Authorization", "${scheme.replaceFirstChar { it.uppercase() }} $token")
                .build()
        }

        return chain.proceed(request)
    }
}
