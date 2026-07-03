package it.roadies.android_app.viewmodel.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.roadies.android_app.client.models.chat.ConversationRequestDTO
import it.roadies.android_app.client.models.user.UserProfileResponseDTO
import it.roadies.android_app.repository.ChatRepository
import it.roadies.android_app.repository.UserRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UserProfileState(
    val isLoading: Boolean = false,
    val user: UserProfileResponseDTO? = null,
    val isOrganizer: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class UserProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val _state = MutableStateFlow(UserProfileState())
    val state: StateFlow<UserProfileState> = _state.asStateFlow()

    private val _navigateToChatEvent = MutableSharedFlow<String>()
    val navigateToChatEvent: SharedFlow<String> = _navigateToChatEvent.asSharedFlow()

    fun loadProfile(username: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            val profileDeferred = async { userRepository.searchUsers(username) }
            val organizerDeferred = async { userRepository.checkIsOrganizer(username) }

            val profileResponse = profileDeferred.await()
            val organizerResponse = organizerDeferred.await()

            if (profileResponse.success && profileResponse.data != null) {
                val exactMatch = profileResponse.data.firstOrNull { it.username == username }
                    ?: profileResponse.data.firstOrNull()

                val isOrg =
                    if (organizerResponse.success) organizerResponse.data ?: false else false

                _state.update {
                    it.copy(
                        isLoading = false,
                        user = exactMatch,
                        isOrganizer = isOrg
                    )
                }
            } else {
                _state.update { it.copy(isLoading = false, error = "Profilo non trovato") }
            }
        }
    }

    fun contactOrganizer() {
        val username = _state.value.user?.username ?: return

        // Tutto dentro il launch, così possiamo usare le funzioni suspend
        viewModelScope.launch {
            // 1. Recupero utente locale (nella coroutine, niente più errore Kotlin!)
            val localUser = userRepository.getCurrentUser() ?: return@launch

            // 2. Recupero ID organizzatore tramite il nuovo endpoint
            val response = userRepository.getUserIdByUsername(username)

            if (response.success && response.data != null) {

                val organizerId = response.data

                val request = ConversationRequestDTO(
                    travelerId = localUser.id,
                    organizerId = organizerId
                )

                val chatResponse = chatRepository.startConversation(request)

                if (chatResponse.success && chatResponse.data != null) {
                    _navigateToChatEvent.emit(chatResponse.data.id)
                } else {
                    _state.update { it.copy(error = "Impossibile avviare la chat") }
                }
            } else {
                _state.update { it.copy(error = "Errore nel recupero ID organizzatore") }
            }
        }
    }
}