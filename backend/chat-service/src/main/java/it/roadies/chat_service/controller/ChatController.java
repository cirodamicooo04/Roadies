package it.roadies.chat_service.controller;

import it.roadies.chat_service.data.dto.request.MessageRequestDTO;
import it.roadies.chat_service.data.dto.response.MessageResponseDTO;
import it.roadies.chat_service.services.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/v1/conversations")
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final MessageService messageService;

    @MessageMapping("/chat.sendMessage")
    public void processMessage(@Payload MessageRequestDTO request, Principal principal) {
        String senderId = principal.getName();
        String senderName = "Utente";

        if (principal instanceof JwtAuthenticationToken jwtAuth) {
            senderName = (String) jwtAuth.getTokenAttributes().getOrDefault("name",
                    jwtAuth.getTokenAttributes().getOrDefault("preferred_username", "Utente"));
        }

        MessageResponseDTO savedMessage = messageService.sendMessage(request, senderId);
        savedMessage.setSenderName(senderName);

        messagingTemplate.convertAndSend(
                "/topic/conversation/" + request.getConversationId(),
                savedMessage
        );
    }
}
