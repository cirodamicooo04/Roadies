package it.roadies.android_app.client.apis.chat

import it.roadies.android_app.client.models.chat.ConversationRequestDTO
import it.roadies.android_app.client.models.chat.ConversationResponseDTO
import it.roadies.android_app.client.models.chat.MessageRequestDTO
import it.roadies.android_app.client.models.chat.MessageResponseDTO
import it.roadies.android_app.client.models.travel.PageResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ChatApi {

    @POST("api/v1/conversations")
    suspend fun startConversation(@Body request: ConversationRequestDTO): Response<ConversationResponseDTO>

    @GET("api/v1/conversations/me")
    suspend fun getMyConversations(): Response<List<ConversationResponseDTO>>
    @GET("api/v1/conversations/{conversationId}/messages")
    suspend fun getMessages(
        @Path("conversationId") conversationId: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<PageResponse<MessageResponseDTO>>

    @POST("api/v1/conversations/{conversationId}/messages")
    suspend fun sendMessage(
        @Path("conversationId") conversationId: String,
        @Body request: MessageRequestDTO
    ): Response<MessageResponseDTO>
}