package it.roadies.android_app.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.roadies.android_app.client.models.travel.FavouriteListResponse
import it.roadies.android_app.repository.FavouriteRepository
import it.roadies.android_app.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FavouriteListsState(
    val isLoading: Boolean = false,
    val lists: List<FavouriteListResponse> = emptyList(),
    val isMyProfile: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class FavouriteListsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val favouriteRepository: FavouriteRepository,
    private val userRepository: UserRepository
) : ViewModel() {
    private val targetUserId: String? = savedStateHandle["userId"]

    private val _uiState = MutableStateFlow(FavouriteListsState())
    val uiState = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        if (targetUserId.isNullOrEmpty()) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            val currentUser = userRepository.getCurrentUser()

            val isMe = currentUser?.id == targetUserId || currentUser?.username == targetUserId

            val actualKeycloakId = if (isMe) currentUser?.id else {
                val idResp = userRepository.getUserIdByUsername(targetUserId)
                if (idResp.success) idResp.data else targetUserId
            }

            if (actualKeycloakId == null) {
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = "Utente non trovato")
                return@launch
            }

            _uiState.value = _uiState.value.copy(isMyProfile = isMe)

            val response = if (isMe) {
                favouriteRepository.getMyLists()
            } else {
                favouriteRepository.getUserLists(actualKeycloakId)
            }

            if (response.success && response.data != null) {
                _uiState.value = _uiState.value.copy(isLoading = false, lists = response.data)
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = response.errorMessage ?: "Errore caricamento liste"
                )
            }
        }
    }
}
