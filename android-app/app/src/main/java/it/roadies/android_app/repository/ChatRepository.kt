package it.roadies.android_app.repository

import it.roadies.android_app.client.apis.chat.ChatApi
import it.roadies.android_app.client.models.chat.ConversationRequestDTO
import it.roadies.android_app.client.models.chat.ConversationResponseDTO
import it.roadies.android_app.client.models.chat.MessageRequestDTO
import it.roadies.android_app.client.models.chat.MessageResponseDTO
import it.roadies.android_app.client.models.travel.PageResponse
import it.roadies.android_app.repository.utils.ApiResponse
import it.roadies.android_app.repository.utils.safeApiCall
import javax.inject.Inject

class ChatRepository @Inject constructor(
    private val chatApi: ChatApi
) {
    suspend fun startConversation(request: ConversationRequestDTO): ApiResponse<ConversationResponseDTO> {
        return safeApiCall { chatApi.startConversation(request) }
    }
    suspend fun getMessages(conversationId: String, page: Int = 0, size: Int = 50): ApiResponse<PageResponse<MessageResponseDTO>> {
        return safeApiCall { chatApi.getMessages(conversationId, page, size) }
    }

    suspend fun sendMessage(conversationId: String, request: MessageRequestDTO): ApiResponse<MessageResponseDTO> {
        return safeApiCall { chatApi.sendMessage(conversationId, request) }
    }

    suspend fun getMyConversations(): ApiResponse<List<ConversationResponseDTO>> {
        return safeApiCall { chatApi.getMyConversations() }
    }
}