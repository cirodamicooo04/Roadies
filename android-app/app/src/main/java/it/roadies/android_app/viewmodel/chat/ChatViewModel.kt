package it.roadies.android_app.viewmodel.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.roadies.android_app.client.models.chat.MessageResponseDTO
import it.roadies.android_app.repository.ChatRepository
import it.roadies.android_app.repository.UserRepository
import it.roadies.android_app.client.websocket.ChatWebSocketManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

import it.roadies.android_app.repository.AuthRepository

data class ChatState(
    val isLoading: Boolean = true,
    val isSending: Boolean = false,
    val messages: List<MessageResponseDTO> = emptyList(),
    val currentUserId: String = "",
    val error: String? = null
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository,
    private val webSocketManager: ChatWebSocketManager
) : ViewModel() {

    private val _state = MutableStateFlow(ChatState())
    val state: StateFlow<ChatState> = _state.asStateFlow()

    private var currentConversationId: String? = null

    fun initChat(conversationId: String) {
        currentConversationId = conversationId

        viewModelScope.launch {
            val localUser = userRepository.getCurrentUser()
            val myId = localUser?.id ?: ""
            _state.update { it.copy(currentUserId = myId) }

            loadMessages(conversationId)

            val token = authRepository.getAuthState().accessToken

            if (token != null && token.isNotBlank()) {
                webSocketManager.connect(
                    token = token,
                    conversationId = conversationId,
                    onMessageReceived = { newMessage ->
                        viewModelScope.launch(Dispatchers.Main) {
                            _state.update { currentState ->
                                currentState.copy(
                                    messages = currentState.messages + newMessage
                                )
                            }
                        }
                    }
                )
            } else {
                _state.update { it.copy(error = "Impossibile autenticare la chat: Token mancante") }
            }
        }
    }

    private suspend fun loadMessages(convId: String) {
        _state.update { it.copy(isLoading = true, error = null) }
        val response = chatRepository.getMessages(convId, 0, 50)

        val data = response.data

        if (response.success && data != null) {
            val sortedMessages = data.content.sortedBy { it.timestamp }
            _state.update { it.copy(isLoading = false, messages = sortedMessages) }
        } else {
            _state.update { it.copy(isLoading = false, error = "Errore nel caricamento dei messaggi") }
        }
    }

    fun sendMessage(content: String) {
        val convId = currentConversationId ?: return
        val sender = _state.value.currentUserId

        if (content.isBlank() || sender.isBlank()) return

        _state.update { it.copy(error = null) }

        webSocketManager.sendMessage(
            conversationId = convId,
            content = content.trim()
        )
    }

    override fun onCleared() {
        super.onCleared()
        webSocketManager.disconnect()
    }
}