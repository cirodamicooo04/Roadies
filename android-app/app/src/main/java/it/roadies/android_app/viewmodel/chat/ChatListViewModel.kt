package it.roadies.android_app.viewmodel.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.roadies.android_app.client.models.chat.ConversationResponseDTO
import it.roadies.android_app.client.models.user.MinimalInformationResponseDTO
import it.roadies.android_app.repository.ChatRepository
import it.roadies.android_app.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChatListState(
    val isLoading: Boolean = true,
    val conversations: List<ConversationResponseDTO> = emptyList(),
    val userInfos: Map<String, MinimalInformationResponseDTO> = emptyMap(),
    val myUserId: String = "",
    val error: String? = null
)

@HiltViewModel
class ChatListViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ChatListState())
    val state: StateFlow<ChatListState> = _state.asStateFlow()

    fun loadConversations() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            val response = chatRepository.getMyConversations()
            val myId = userRepository.getCurrentUser()?.id ?: ""

            if (response.success && response.data != null) {
                val conversations = response.data

                // Estrai gli ID degli interlocutori
                val otherUserIds = conversations.map {
                    if (it.travelerId == myId) it.organizerId else it.travelerId
                }.distinct().filterNotNull()

                // Recupero batch dal backend
                val infoResponse = userRepository.getOrganizersInfo(otherUserIds)

                val userInfosMap = if (infoResponse.success && infoResponse.data != null) {
                    infoResponse.data.associateBy { it.keycloakId ?: "" }
                } else {
                    emptyMap()
                }

                _state.update {
                    it.copy(
                        isLoading = false,
                        conversations = conversations,
                        userInfos = userInfosMap,
                        myUserId = myId
                    )
                }
            } else {
                _state.update { it.copy(isLoading = false, error = "Errore nel caricamento delle chat") }
            }
        }
    }
}