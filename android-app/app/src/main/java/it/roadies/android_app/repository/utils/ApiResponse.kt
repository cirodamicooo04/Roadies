package it.roadies.android_app.repository.utils

data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val errorMessage: String? = null,
    val statusCode: Int? = null
)
