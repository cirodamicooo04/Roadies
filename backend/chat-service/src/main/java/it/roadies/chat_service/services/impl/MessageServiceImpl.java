package it.roadies.chat_service.services.impl;

import it.roadies.chat_service.data.dto.request.MessageRequestDTO;
import it.roadies.chat_service.data.dto.response.MessageResponseDTO;
import it.roadies.chat_service.data.entities.Conversation;
import it.roadies.chat_service.data.entities.Message;
import it.roadies.chat_service.data.mapper.MessageMapper;
import it.roadies.chat_service.data.repositories.ConversationRepository;
import it.roadies.chat_service.data.repositories.MessageRepository;
import it.roadies.chat_service.exception.ConversationNotFoundException;
import it.roadies.chat_service.services.MessageService;
import it.roadies.chat_service.utilities.EncryptionUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final MessageMapper messageMapper;
    private final EncryptionUtil encryptionUtil;

    @Override
    @Transactional
    public MessageResponseDTO sendMessage(MessageRequestDTO request, String senderId) {
        Conversation conversation = conversationRepository.findById(request.getConversationId())
                .orElseThrow(() -> new ConversationNotFoundException(
                        "Conversazione non trovata con id: " + request.getConversationId()));

        validateParticipant(conversation, senderId);

        String encryptedContent = encryptionUtil.encrypt(request.getContent());

        Message message = Message.builder()
                .conversation(conversation)
                .senderId(senderId)
                .content(encryptedContent)
                .build();

        Message savedMessage = messageRepository.save(message);

        MessageResponseDTO response = messageMapper.toDto(savedMessage);
        // Restituiamo il contenuto in chiaro nella response (non quello criptato dal DB)
        response.setContent(request.getContent());

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MessageResponseDTO> getConversationHistory(UUID conversationId, String userId, int page, int size) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ConversationNotFoundException(
                        "Conversazione non trovata con id: " + conversationId));

        validateParticipant(conversation, userId);

        Pageable pageable = PageRequest.of(page, size);
        Page<Message> messagesPage = messageRepository.findByConversationIdOrderByTimestampDesc(conversationId, pageable);

        return messagesPage.map(message -> {
            MessageResponseDTO dto = messageMapper.toDto(message);
            try {
                dto.setContent(encryptionUtil.decrypt(message.getContent()));
            } catch (Exception e) {
                dto.setContent("[Errore decrittografia]");
            }
            return dto;
        });
    }

    @Override
    @Transactional
    public void markMessagesAsRead(UUID conversationId, String userId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ConversationNotFoundException(
                        "Conversazione non trovata con id: " + conversationId));

        validateParticipant(conversation, userId);

        messageRepository.markAllAsRead(conversationId, userId);
    }

    private void validateParticipant(Conversation conversation, String userId) {
        if (!userId.equals(conversation.getTravelerId()) && !userId.equals(conversation.getOrganizerId())) {
            throw new IllegalArgumentException("L'utente non è autorizzato ad accedere a questa conversazione");
        }
    }
}
