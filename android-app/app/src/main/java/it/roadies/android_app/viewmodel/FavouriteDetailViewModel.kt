package it.roadies.android_app.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.roadies.android_app.client.models.travel.FavouriteListResponse
import it.roadies.android_app.client.models.travel.FavouriteListUpdateRequest
import it.roadies.android_app.repository.FavouriteRepository
import it.roadies.android_app.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

import it.roadies.android_app.repository.FriendshipRepository
import it.roadies.android_app.client.models.user.UserProfileResponseDTO

data class FavouriteListDetailState(
    val isLoading: Boolean = false,
    val list: FavouriteListResponse? = null,
    val isOwner: Boolean = false,
    val errorMessage: String? = null,
    val deleteSuccess: Boolean = false,
    val friendsList: List<UserProfileResponseDTO> = emptyList(),
    val isFriendsLoading: Boolean = false,
    val sharedWithUsernames: List<String> = emptyList()
)

@HiltViewModel
class FavouriteListDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val favouriteRepository: FavouriteRepository,
    private val userRepository: UserRepository,
    private val authRepository: it.roadies.android_app.repository.AuthRepository,
    private val friendshipRepository: FriendshipRepository
) : ViewModel() {

    private val listIdStr: String? = savedStateHandle["listId"]
    private val _uiState = MutableStateFlow(FavouriteListDetailState())
    val uiState = _uiState.asStateFlow()

    init {
        loadList()
    }

    fun loadList() {
        val uuid = runCatching { UUID.fromString(listIdStr) }.getOrNull() ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val response = favouriteRepository.getListDetail(uuid)

            if (response.success && response.data != null) {
                val authState = authRepository.getAuthState()
                val claims = authState.accessToken?.let { authRepository.getUserClaims(it) }
                val keycloakId = claims?.sub

                val currentUser = userRepository.getCurrentUser()
                val isOwner = currentUser?.id == response.data.ownerId || keycloakId == response.data.ownerId
                
                var sharedUsernames = emptyList<String>()
                val sharedIds = response.data.sharedWithIds
                if (!sharedIds.isNullOrEmpty()) {
                    val minimalInfoRes = userRepository.getMinimalInformation(sharedIds)
                    if (minimalInfoRes.success && minimalInfoRes.data != null) {
                        sharedUsernames = minimalInfoRes.data.mapNotNull { it.username }
                    }
                }

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    list = response.data,
                    isOwner = isOwner,
                    sharedWithUsernames = sharedUsernames
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = response.errorMessage ?: "Errore caricamento lista"
                )
            }
        }
    }

    fun loadFriends() {
        if (_uiState.value.friendsList.isNotEmpty()) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isFriendsLoading = true)
            val response = friendshipRepository.getFriendsList()
            if (response.success && response.data != null) {
                _uiState.value = _uiState.value.copy(isFriendsLoading = false, friendsList = response.data)
            } else {
                _uiState.value = _uiState.value.copy(isFriendsLoading = false)
            }
        }
    }

    fun toggleFriendShare(username: String, isShared: Boolean) {
        val listUuid = runCatching { UUID.fromString(listIdStr) }.getOrNull() ?: return
        
        viewModelScope.launch {
            val userResponse = userRepository.getUserIdByUsername(username)
            if (!userResponse.success || userResponse.data == null) {
                _uiState.value = _uiState.value.copy(errorMessage = "Errore nel recupero ID utente")
                return@launch
            }
            val friendId = userResponse.data

            val response = if (isShared) {
                favouriteRepository.addFriendToList(listUuid, friendId)
            } else {
                favouriteRepository.removeFriendFromList(listUuid, friendId)
            }

            if (response.success) {
                // Aggiorna lo stato locale per riflettere il cambiamento immediatamente
                val currentSharedUsernames = _uiState.value.sharedWithUsernames.toMutableList()
                if (isShared) {
                    if (!currentSharedUsernames.contains(username)) currentSharedUsernames.add(username)
                } else {
                    currentSharedUsernames.remove(username)
                }
                _uiState.value = _uiState.value.copy(sharedWithUsernames = currentSharedUsernames)
            } else {
                _uiState.value = _uiState.value.copy(errorMessage = response.errorMessage ?: "Errore modifica condivisione")
            }
        }
    }

    fun deleteList() {
        val uuid = runCatching { UUID.fromString(listIdStr) }.getOrNull() ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val response = favouriteRepository.deleteList(uuid)
            if (response.success) {
                _uiState.value = _uiState.value.copy(isLoading = false, deleteSuccess = true)
            } else {
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = response.errorMessage)
            }
        }
    }

    fun updateList(name: String, visibility: FavouriteListResponse.Visibility) {
        val uuid = runCatching { UUID.fromString(listIdStr) }.getOrNull() ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val request = FavouriteListUpdateRequest(name = name, visibility = visibility)
            val response = favouriteRepository.updateList(uuid, request)

            if (response.success && response.data != null) {
                _uiState.value = _uiState.value.copy(isLoading = false, list = response.data)
            } else {
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = response.errorMessage)
            }
        }
    }

    fun removeTravel(travelId: UUID) {
        val uuid = runCatching { UUID.fromString(listIdStr) }.getOrNull() ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val response = favouriteRepository.removeTravelFromList(uuid, travelId)
            if (response.success) {
                loadList()
            } else {
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = response.errorMessage)
            }
        }
    }

    fun removeActivity(activityId: UUID) {
        val uuid = runCatching { UUID.fromString(listIdStr) }.getOrNull() ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val response = favouriteRepository.removeActivityFromList(uuid, activityId)
            if (response.success) {
                loadList()
            } else {
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = response.errorMessage)
            }
        }
    }

}
