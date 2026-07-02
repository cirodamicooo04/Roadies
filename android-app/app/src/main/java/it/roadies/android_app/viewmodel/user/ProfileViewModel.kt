package it.roadies.android_app.viewmodel.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.roadies.android_app.model.User
import it.roadies.android_app.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileState(
    val isLoading: Boolean = false,
    val profile: User? = null,
    val error: String? = null,
    val warningMessage: String? = null
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: UserRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state.asStateFlow()

    init {
        observeLocalProfile()
        loadProfile()
    }

    private fun observeLocalProfile() {
        viewModelScope.launch {
            repository.observeCurrentUser().collect { user ->
                _state.update { current ->
                    current.copy(profile = user)
                }
            }
        }
    }

    fun loadProfile() {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isLoading = true,
                    error = null,
                    warningMessage = null
                )
            }

            val localUser = repository.getCurrentUser()

            if (localUser == null) {
                val response = repository.getProfile()

                _state.update { current ->
                    current.copy(
                        isLoading = false,
                        error = response.errorMessage,
                        warningMessage = null
                    )
                }
            } else {
                _state.update {
                    it.copy(isLoading = false)
                }

                val response = repository.getProfile()

                if (!response.success) {
                    _state.update { current ->
                        current.copy(
                            warningMessage = response.errorMessage
                        )
                    }
                }
            }
        }
    }
}