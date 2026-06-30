package it.roadies.android_app.repository.utils

import it.roadies.android_app.client.infrastructure.getErrorResponse
import it.roadies.android_app.client.models.travel.ErrorResponse
import retrofit2.Response
import java.io.EOFException
import java.io.IOException

suspend fun <T> safeApiCall(
    call: suspend () -> Response<T>
): ApiResponse<T> {
    return try {
        val response = call()

        if (response.isSuccessful) {
            ApiResponse(
                success = true,
                data = response.body(),
                statusCode = response.code()
            )
        } else {
            val errorMessage = runCatching {
                response.getErrorResponse<ErrorResponse>()?.message
            }.getOrNull()

            ApiResponse(
                success = false,
                errorMessage = errorMessage ?: response.message(),
                statusCode = response.code()
            )
        }
    } catch (e: EOFException) {
        // Retrofit lancia EOFException se il server restituisce 204 No Content
        ApiResponse(
            success = true,
            data = null,
            statusCode = 204
        )
    } catch (e: IOException) {
        ApiResponse(
            success = false,
            errorMessage = "Connessione non disponibile"
        )
    } catch (e: Exception) {
        ApiResponse(
            success = false,
            errorMessage = e.message ?: "Errore sconosciuto"
        )
    }
}
