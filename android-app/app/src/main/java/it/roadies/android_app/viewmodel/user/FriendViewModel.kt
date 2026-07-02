package it.roadies.android_app.viewmodel.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.roadies.android_app.client.apis.user.FriendshipManagementApi.StatusRespond
import it.roadies.android_app.client.models.user.FriendshipResponseDTO
import it.roadies.android_app.client.models.user.UserProfileResponseDTO
import it.roadies.android_app.repository.FriendshipRepository
import it.roadies.android_app.repository.UserRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class FriendState(
    val isLoading: Boolean = false,
    val friends: List<UserProfileResponseDTO> = emptyList(),
    val error: String? = null,
    val searchQuery: String = "",
    val searchResults: List<UserProfileResponseDTO> = emptyList(),
    val isSearching: Boolean = false,
    val requestSentTo: Set<String> = emptySet(),
    val pendingRequests: List<FriendshipResponseDTO> = emptyList()
)

@HiltViewModel
class FriendViewModel @Inject constructor(
    private val repository: FriendshipRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _state = MutableStateFlow(FriendState())
    val state: StateFlow<FriendState> = _state.asStateFlow()

    val friendIds: List<String> get() = state.value.friends.mapNotNull { it.username }
    private var searchJob: Job? = null

    init {
        loadFriends()
        loadSentRequests()
        loadPendingRequests()
    }

    fun loadFriends() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val response = repository.getFriendsList()
            if (response.success && response.data != null) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        friends = response.data,
                        error = null
                    )
                }
            } else {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = response.errorMessage
                    )
                }
            }
        }
    }

    private fun loadSentRequests() {
        viewModelScope.launch {
            val response = repository.getSentRequests()
            if (response.success && response.data != null) {
                val sentUsernames = response.data
                    .mapNotNull { it.friendProfile?.username }
                    .toSet()

                _state.update {
                    it.copy(
                        requestSentTo = sentUsernames,
                        error = null
                    )
                }
            }
        }
    }

    fun loadPendingRequests() {
        viewModelScope.launch {
            val response = repository.getPendingRequests()
            if (response.success && response.data != null) {
                _state.update {
                    it.copy(
                        pendingRequests = response.data,
                        error = null
                    )
                }
            }
        }
    }

    fun respondToRequest(friendshipId: UUID, accept: Boolean) {
        viewModelScope.launch {
            val status = if (accept) StatusRespond.ACCEPTED else StatusRespond.REJECTED
            val response = repository.respondToRequest(friendshipId, status)

            if (response.success) {
                _state.update {
                    it.copy(
                        pendingRequests = it.pendingRequests.filter { request -> request.id != friendshipId },
                        error = null
                    )
                }

                if (accept) {
                    loadFriends()
                }
            } else {
                _state.update {
                    it.copy(error = "Errore nella risposta alla richiesta")
                }
            }
        }
    }

    fun searchUser() {
        val query = state.value.searchQuery.trim()
        if (query.isBlank()) return

        viewModelScope.launch {
            _state.update { it.copy(isSearching = true, error = null) }
            val response = userRepository.searchUsers(query)

            if (response.success && response.data != null) {
                _state.update {
                    it.copy(
                        isSearching = false,
                        searchResults = response.data,
                        error = null
                    )
                }
            } else {
                _state.update {
                    it.copy(
                        isSearching = false,
                        error = response.errorMessage
                    )
                }
            }
        }
    }

    fun onSearchQueryChange(newQuery: String) {
        _state.update { it.copy(searchQuery = newQuery) }
        searchJob?.cancel()

        if (newQuery.length >= 3) {
            searchJob = viewModelScope.launch {
                delay(500)
                searchUser()
            }
        } else {
            _state.update {
                it.copy(
                    searchResults = emptyList(),
                    isSearching = false
                )
            }
        }
    }

    fun sendFriendRequest(username: String) {
        viewModelScope.launch {
            val response = repository.sendFriendshipRequest(username)
            if (response.success) {
                _state.update {
                    it.copy(
                        requestSentTo = it.requestSentTo + username,
                        error = null
                    )
                }
            } else {
                _state.update {
                    it.copy(error = "Errore nell'invio della richiesta a $username")
                }
            }
        }
    }

    fun removeFriend(friendUsername: String) {
        viewModelScope.launch {
            val response = repository.removeFriend(friendUsername)
            if (response.success) {
                _state.update {
                    it.copy(
                        friends = it.friends.filterNot { friend -> friend.username == friendUsername },
                        error = null
                    )
                }
            } else {
                _state.update {
                    it.copy(error = response.errorMessage ?: "Errore nella rimozione dell'amico")
                }
            }
        }
    }
}