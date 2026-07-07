package it.roadies.android_app.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.roadies.android_app.client.models.travel.FavouriteListResponse
import it.roadies.android_app.model.FavouriteList
import it.roadies.android_app.repository.FavouriteRepository
import it.roadies.android_app.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FavouriteListsState(
    val isLoading: Boolean = false,
    val lists: List<FavouriteListResponse> = emptyList(),
    val cachedLists: List<FavouriteList> = emptyList(),
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
            val isMe = currentUser != null &&
                    (currentUser.id == targetUserId || currentUser.username == targetUserId)

            _uiState.value = _uiState.value.copy(isMyProfile = isMe)

            if (isMe) {
                // Osserva la cache Room in real-time
                launch {
                    favouriteRepository.observeMyLists(currentUser.id).collectLatest { cached ->
                        _uiState.value = _uiState.value.copy(cachedLists = cached)
                    }
                }
                // Scarica dal network e aggiorna la cache
                val response = favouriteRepository.refreshMyLists(currentUser.id)
                if (response.success && response.data != null) {
                    _uiState.value = _uiState.value.copy(isLoading = false, lists = response.data)
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = response.errorMessage ?: "Errore caricamento liste"
                    )
                }
            } else {
                val actualKeycloakId = if (isMe) currentUser?.id else {
                    val idResp = userRepository.getUserIdByUsername(targetUserId)
                    if (idResp.success) idResp.data else targetUserId
                }
                if (actualKeycloakId == null) {
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = "Utente non trovato")
                    return@launch
                }
                val response = favouriteRepository.getUserLists(actualKeycloakId)
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
}
