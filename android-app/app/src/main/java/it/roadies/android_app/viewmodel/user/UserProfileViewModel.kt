package it.roadies.android_app.viewmodel.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.roadies.android_app.client.models.user.UserProfileResponseDTO
import it.roadies.android_app.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UserProfileState(
    val isLoading: Boolean = false,
    val user: UserProfileResponseDTO? = null,
    val error: String? = null
)

@HiltViewModel
class UserProfileViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _state = MutableStateFlow(UserProfileState())
    val state: StateFlow<UserProfileState> = _state.asStateFlow()

    fun loadProfile(username: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val response = userRepository.searchUsers(username)
            if (response.success && !response.data.isNullOrEmpty()) {
                val exactMatch = response.data.firstOrNull { it.username == username }
                    ?: response.data.first()
                _state.update { it.copy(isLoading = false, user = exactMatch) }
            } else {
                _state.update { it.copy(isLoading = false, error = "Profilo non trovato") }
            }
        }
    }
}