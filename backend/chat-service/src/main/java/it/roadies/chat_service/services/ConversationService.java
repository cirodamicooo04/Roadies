package it.roadies.chat_service.services;

import it.roadies.chat_service.data.dto.response.ConversationResponseDTO;
import it.roadies.chat_service.data.entities.Conversation;

import java.util.List;

public interface ConversationService {

    Conversation createOrGetConversation(String travelerId, String organizerId);

    List<ConversationResponseDTO> getUserConversations(String userId);
}