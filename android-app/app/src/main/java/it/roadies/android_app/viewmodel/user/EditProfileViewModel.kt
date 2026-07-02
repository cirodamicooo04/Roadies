package it.roadies.android_app.viewmodel.user

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.roadies.android_app.client.models.user.UserUpdateRequestDTO
import it.roadies.android_app.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class EditProfileUiState(
    val firstName: String = "",
    val lastName: String = "",
    val avatarUrl: String = "",
    val birthDate: LocalDate? = null,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _state = MutableStateFlow(EditProfileUiState())
    val state: StateFlow<EditProfileUiState> = _state.asStateFlow()

    private var selectedAvatarUri: Uri? = null

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isLoading = true,
                    error = null,
                    successMessage = null
                )
            }

            val localUser = userRepository.getCurrentUser()

            if (localUser != null) {
                _state.update {
                    it.copy(
                        firstName = localUser.firstName,
                        lastName = localUser.lastName,
                        avatarUrl = localUser.avatarUrl,
                        birthDate = localUser.birthDate,
                        isLoading = false
                    )
                }
            }

            val remoteResponse = userRepository.getProfile()

            if (remoteResponse.success && remoteResponse.data != null) {
                val profile = remoteResponse.data
                val updatedLocal = userRepository.getCurrentUser()

                _state.update {
                    it.copy(
                        firstName = profile.firstName ?: updatedLocal?.firstName.orEmpty(),
                        lastName = profile.lastName ?: updatedLocal?.lastName.orEmpty(),
                        avatarUrl = profile.avatarUrl ?: updatedLocal?.avatarUrl.orEmpty(),
                        birthDate = updatedLocal?.birthDate ?: it.birthDate,
                        isLoading = false,
                        error = null
                    )
                }
            } else {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = "Errore durante il caricamento del profilo"
                    )
                }
            }
        }
    }

    fun onFirstNameChange(value: String) {
        _state.update {
            it.copy(
                firstName = value,
                error = null,
                successMessage = null
            )
        }
    }

    fun onLastNameChange(value: String) {
        _state.update {
            it.copy(
                lastName = value,
                error = null,
                successMessage = null
            )
        }
    }

    fun onBirthDateChange(value: LocalDate?) {
        _state.update {
            it.copy(
                birthDate = value,
                error = null,
                successMessage = null
            )
        }
    }

    fun onAvatarPicked(uri: Uri?) {
        selectedAvatarUri = uri
        _state.update {
            it.copy(
                avatarUrl = uri?.toString().orEmpty(),
                error = null,
                successMessage = null
            )
        }
    }

    fun saveProfile() {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isSaving = true,
                    error = null,
                    successMessage = null
                )
            }

            try {
                val profileResponse = userRepository.updateProfile(
                    updateRequest = UserUpdateRequestDTO(
                        firstName = _state.value.firstName,
                        lastName = _state.value.lastName,
                        birthDate = _state.value.birthDate,
                        avatarUrl = null
                    ),
                    localBirthDate = _state.value.birthDate,
                    localAvatarUrl = _state.value.avatarUrl
                )

                if (!profileResponse.success || profileResponse.data == null) {
                    _state.update {
                        it.copy(
                            isSaving = false,
                            error = "Errore durante aggiornamento profilo"
                        )
                    }
                    return@launch
                }

                var finalAvatarUrl = profileResponse.data.avatarUrl ?: _state.value.avatarUrl

                selectedAvatarUri?.let { uri ->
                    val avatarResponse = userRepository.uploadAvatar(uri)

                    if (!avatarResponse.success || avatarResponse.data == null) {
                        _state.update {
                            it.copy(
                                isSaving = false,
                                error = "Errore durante upload avatar"
                            )
                        }
                        return@launch
                    }

                    finalAvatarUrl = avatarResponse.data.avatarUrl ?: finalAvatarUrl
                    selectedAvatarUri = null
                }

                _state.update {
                    it.copy(
                        isSaving = false,
                        avatarUrl = finalAvatarUrl,
                        successMessage = "Profilo aggiornato con successo",
                        error = null
                    )
                }

            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isSaving = false,
                        error = e.message ?: "Errore durante il salvataggio"
                    )
                }
            }
        }
    }
}