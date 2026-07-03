package it.roadies.chat_service.services;

import it.roadies.chat_service.data.dto.request.MessageRequestDTO;
import it.roadies.chat_service.data.dto.response.MessageResponseDTO;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface MessageService {

    MessageResponseDTO sendMessage(MessageRequestDTO request, String senderId);

    Page<MessageResponseDTO> getConversationHistory(UUID conversationId, String userId, int page, int size);

    void markMessagesAsRead(UUID conversationId, String userId);
}
