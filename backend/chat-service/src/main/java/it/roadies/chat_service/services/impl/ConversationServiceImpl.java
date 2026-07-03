package it.roadies.chat_service.services.impl;

import it.roadies.chat_service.data.dto.response.ConversationResponseDTO;
import it.roadies.chat_service.data.entities.Conversation;
import it.roadies.chat_service.data.mapper.ConversationMapper;
import it.roadies.chat_service.data.repositories.ConversationRepository;
import it.roadies.chat_service.services.ConversationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConversationServiceImpl implements ConversationService {

    private final ConversationRepository conversationRepository;
    private final ConversationMapper conversationMapper;

    @Override
    @Transactional
    public Conversation createOrGetConversation(String travelerId, String organizerId) {
        return conversationRepository
                .findByTravelerIdAndOrganizerId(travelerId, organizerId)
                .orElseGet(() -> {
                    Conversation newConversation = Conversation.builder()
                            .travelerId(travelerId)
                            .organizerId(organizerId)
                            .build();
                    return conversationRepository.save(newConversation);
                });
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConversationResponseDTO> getUserConversations(String userId) {
        return conversationRepository.findByTravelerIdOrOrganizerId(userId, userId)
                .stream()
                .map(conversation -> {
                    ConversationResponseDTO dto = conversationMapper.toDto(conversation);
                    long unreadCount = conversation.getMessages().stream()
                            .filter(m -> !m.isRead() && !m.getSenderId().equals(userId))
                            .count();
                    dto.setUnreadCount((int) unreadCount);
                    return dto;
                })
                .collect(Collectors.toList());
    }
}