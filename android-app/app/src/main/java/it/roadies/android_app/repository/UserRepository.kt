package it.roadies.android_app.repository

import android.util.Patterns
import it.roadies.android_app.client.apis.user.UserManagementApi
import it.roadies.android_app.client.models.user.UserProfileResponseDTO
import it.roadies.android_app.client.models.user.UserSyncRequestDTO
import it.roadies.android_app.client.models.user.UserUpdateRequestDTO
import it.roadies.android_app.model.User
import it.roadies.android_app.model.dao.UserDao
import it.roadies.android_app.repository.utils.ApiResponse
import it.roadies.android_app.repository.utils.safeApiCall
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

sealed class SyncResult {
    data class NewUser(val profile: UserProfileResponseDTO) : SyncResult()
    data class ExistingUser(val profile: UserProfileResponseDTO) : SyncResult()
    data class Error(val message: String) : SyncResult()
}

class UserRepository @Inject constructor(
    private val userApi: UserManagementApi,
    private val userDao: UserDao
) {
    fun observeCurrentUser(): Flow<User?> = userDao.observeCurrentUser()

    suspend fun getCurrentUser(): User? = userDao.getCurrentUser()

    suspend fun getProfile(): ApiResponse<UserProfileResponseDTO> {
        val response = safeApiCall { userApi.getProfile() }
        if (response.success && response.data != null) {
            mergeProfileIntoLocalUser(response.data)
        }
        return response
    }

    suspend fun syncUser(request: UserSyncRequestDTO): ApiResponse<UserProfileResponseDTO> {
        val response = safeApiCall { userApi.syncUser(request) }
        if (response.success && response.data != null) {
            saveUserFromSync(request, response.data)
        }
        return response
    }

    suspend fun searchUsers(query: String): ApiResponse<List<UserProfileResponseDTO>> =
        safeApiCall { userApi.searchUser(query) }

    suspend fun updateProfile(updateRequest: UserUpdateRequestDTO): ApiResponse<UserProfileResponseDTO> {
        val response = safeApiCall { userApi.updateProfile(updateRequest) }
        if (response.success && response.data != null) {
            mergeProfileIntoLocalUser(response.data)
        }
        return response
    }

    suspend fun clearLocalUser() {
        userDao.clearAll()
    }

    private suspend fun mergeProfileIntoLocalUser(dto: UserProfileResponseDTO) {
        val existing = userDao.getCurrentUser()

        if (existing != null) {
            val updatedUser = existing.copy(
                firstName = dto.firstName.safeFirstName(existing.firstName),
                lastName = dto.lastName.safeLastName(existing.lastName),
                username = dto.username.safeUsername(existing.username),
                avatarUrl = dto.avatarUrl ?: existing.avatarUrl,
                points = dto.points ?: existing.points,
                badge = dto.badge?.toString() ?: existing.badge
            )
            userDao.insert(updatedUser)
            return
        }

        val username = dto.username.safeUsername("user123")
        val firstName = dto.firstName.safeFirstName(username)
        val lastName = dto.lastName.safeLastName("-")
        val email = "placeholder@example.com"

        val newUser = User(
            id = username,
            firstName = firstName,
            lastName = lastName,
            email = email,
            username = username,
            avatarUrl = dto.avatarUrl ?: "",
            birthDate = null,
            points = dto.points ?: 0L,
            badge = dto.badge?.toString() ?: "BRONZE"
        )

        userDao.insert(newUser)
    }

    private suspend fun saveUserFromSync(
        request: UserSyncRequestDTO,
        dto: UserProfileResponseDTO
    ) {
        val existing = userDao.getCurrentUser()

        val safeEmail = request.email.safeEmail()
        val safeUsername = dto.username.safeUsername(
            request.username.safeUsername("user123")
        )
        val safeFirstName = dto.firstName.safeFirstName(
            request.firstName.safeFirstName(safeUsername)
        )
        val safeLastName = dto.lastName.safeLastName(
            request.lastName.safeLastName("-")
        )

        val user = if (existing != null) {
            existing.copy(
                firstName = safeFirstName,
                lastName = safeLastName,
                email = safeEmail,
                username = safeUsername,
                avatarUrl = dto.avatarUrl ?: existing.avatarUrl,
                points = dto.points ?: existing.points,
                badge = dto.badge?.toString() ?: existing.badge
            )
        } else {
            User(
                id = request.keycloakId?.takeIf { it.isNotBlank() } ?: safeUsername,
                firstName = safeFirstName,
                lastName = safeLastName,
                email = safeEmail,
                username = safeUsername,
                avatarUrl = dto.avatarUrl ?: "",
                birthDate = null,
                points = dto.points ?: 0L,
                badge = dto.badge?.toString() ?: "BRONZE"
            )
        }

        userDao.insert(user)
    }

    private fun String?.safeFirstName(fallback: String): String {
        val value = this?.trim().orEmpty()
        return when {
            value.length in 1..30 -> value
            fallback.trim().length in 1..30 -> fallback.trim()
            else -> "User"
        }
    }

    private fun String?.safeLastName(fallback: String): String {
        val value = this?.trim().orEmpty()
        return when {
            value.length in 1..30 -> value
            fallback.trim().length in 1..30 -> fallback.trim()
            else -> "-"
        }
    }

    private fun String?.safeUsername(fallback: String): String {
        val value = this?.trim().orEmpty()
        return when {
            value.length in 3..30 -> value
            fallback.trim().length in 3..30 -> fallback.trim()
            else -> "user123"
        }
    }

    private fun String?.safeEmail(): String {
        val value = this?.trim().orEmpty()
        return if (Patterns.EMAIL_ADDRESS.matcher(value).matches()) {
            value
        } else {
            "placeholder@example.com"
        }
    }
}