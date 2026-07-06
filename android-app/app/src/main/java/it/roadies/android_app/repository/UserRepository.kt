package it.roadies.android_app.repository

import android.content.Context
import android.net.Uri
import android.util.Patterns
import dagger.hilt.android.qualifiers.ApplicationContext
import it.roadies.android_app.client.apis.user.UserManagementApi
import it.roadies.android_app.client.models.user.MinimalInformationResponseDTO
import it.roadies.android_app.client.models.user.UserProfileResponseDTO
import it.roadies.android_app.client.models.user.UserSyncRequestDTO
import it.roadies.android_app.client.models.user.UserUpdateRequestDTO
import it.roadies.android_app.model.User
import it.roadies.android_app.model.dao.UserDao
import it.roadies.android_app.repository.utils.ApiResponse
import it.roadies.android_app.repository.utils.safeApiCall
import kotlinx.coroutines.flow.Flow
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.time.LocalDate
import javax.inject.Inject

sealed class SyncResult {
    data class NewUser(val profile: UserProfileResponseDTO) : SyncResult()
    data class ExistingUser(val profile: UserProfileResponseDTO) : SyncResult()
    data class Error(val message: String) : SyncResult()
}

class UserRepository @Inject constructor(
    private val userApi: UserManagementApi,
    private val userDao: UserDao,
    @ApplicationContext private val context: Context
) {

    fun observeCurrentUser(): Flow<User?> {
        return userDao.observeCurrentUser()
    }

    suspend fun getCurrentUser(): User? {
        return userDao.getCurrentUser()
    }

    suspend fun getProfile(): ApiResponse<UserProfileResponseDTO> {
        val response = safeApiCall {
            userApi.getProfile()
        }

        if (response.success && response.data != null) {
            mergeProfileIntoLocalUser(response.data)
        }

        return response
    }

    suspend fun syncUser(request: UserSyncRequestDTO): ApiResponse<UserProfileResponseDTO> {
        val response = safeApiCall {
            userApi.syncUser(request)
        }

        if (response.success && response.data != null) {
            saveUserFromSync(request, response.data)
        }

        return response
    }

    suspend fun searchUsers(query: String): ApiResponse<List<UserProfileResponseDTO>> {
        return safeApiCall {
            userApi.searchUser(query)
        }
    }

    suspend fun requestOrganizerRole(): ApiResponse<Unit> {
        return safeApiCall {
            userApi.requestOrganizerRole()
        }
    }

    suspend fun updateProfile(
        updateRequest: UserUpdateRequestDTO,
        localBirthDate: LocalDate?,
        localAvatarUrl: String?
    ): ApiResponse<UserProfileResponseDTO> {
        val response = safeApiCall {
            userApi.updateProfile(updateRequest)
        }

        if (response.success && response.data != null) {
            mergeProfileIntoLocalUser(
                dto = response.data,
                forcedBirthDate = localBirthDate,
                forcedAvatarUrl = localAvatarUrl
            )
        }

        return response
    }

    suspend fun uploadAvatar(uri: Uri): ApiResponse<UserProfileResponseDTO> {
        val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            ?: throw IllegalArgumentException("Impossibile leggere il file selezionato")

        val mimeType = context.contentResolver.getType(uri) ?: "image/jpeg"

        val part = MultipartBody.Part.createFormData(
            name = "avatarFile",
            filename = "avatar.jpg",
            body = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
        )

        val response = safeApiCall {
            userApi.uploadAvatar(part)
        }

        if (response.success && response.data != null) {
            mergeProfileIntoLocalUser(
                dto = response.data,
                forcedAvatarUrl = response.data.avatarUrl
            )
        }

        return response
    }

    suspend fun clearLocalUser() {
        userDao.clearAll()
    }

    private suspend fun mergeProfileIntoLocalUser(
        dto: UserProfileResponseDTO,
        forcedBirthDate: LocalDate? = null,
        forcedAvatarUrl: String? = null
    ) {
        val existing = userDao.getCurrentUser()

        val updatedUser = if (existing != null) {
            existing.copy(
                firstName = dto.firstName.safeFirstName(existing.firstName),
                lastName = dto.lastName.safeLastName(existing.lastName),
                username = dto.username.safeUsername(existing.username),
                avatarUrl = forcedAvatarUrl ?: dto.avatarUrl ?: existing.avatarUrl,
                birthDate = forcedBirthDate ?: existing.birthDate,
                points = dto.points ?: existing.points,
                badge = dto.badge?.toString() ?: existing.badge
            )
        } else {
            User(
                id = dto.username ?: "user123",
                firstName = dto.firstName.safeFirstName("User"),
                lastName = dto.lastName.safeLastName("-"),
                username = dto.username.safeUsername("user123"),
                email = "user@roadies.it",
                avatarUrl = forcedAvatarUrl ?: dto.avatarUrl ?: "",
                birthDate = forcedBirthDate,
                points = dto.points ?: 0L,
                badge = dto.badge?.toString() ?: "BRONZE"
            )
        }

        userDao.insert(updatedUser)
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
                birthDate = existing.birthDate,
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

    suspend fun getOrganizersInfo(ids: List<String>): ApiResponse<List<MinimalInformationResponseDTO>> {
        return safeApiCall { userApi.getMinimalInformation(ids) }
    }

    suspend fun getOrganizerInfo(username: String ): ApiResponse<MinimalInformationResponseDTO> {
        return safeApiCall { userApi.getUserMinimalInformation(username) }
    }

    suspend fun checkIsOrganizer(username: String): ApiResponse<Boolean> {
        return safeApiCall {
            userApi.checkIsOrganizer(username)
        }
    }

    suspend fun getUserIdByUsername(username: String): ApiResponse<String> {
        return safeApiCall {
            userApi.getUserIdByUsername(username)
        }
    }

    suspend fun getMinimalInformation(userIds: List<String>): ApiResponse<List<it.roadies.android_app.client.models.user.MinimalInformationResponseDTO>> {
        return safeApiCall {
            userApi.getMinimalInformation(userIds)
        }
    }
}