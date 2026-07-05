package it.roadies.chat_service.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.roadies.chat_service.data.dto.request.ConversationRequestDTO;
import it.roadies.chat_service.data.dto.response.ConversationResponseDTO;
import it.roadies.chat_service.data.dto.response.MessageResponseDTO;
import it.roadies.chat_service.data.mapper.ConversationMapper;
import it.roadies.chat_service.services.ConversationService;
import it.roadies.chat_service.services.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/conversations")
@RequiredArgsConstructor
@Tag(name = "Conversations", description = "API per la gestione delle conversazioni e dello storico messaggi")
public class ConversationController {

    private final ConversationService conversationService;
    private final MessageService messageService;
    private final ConversationMapper conversationMapper;

    @PostMapping
    @Operation(summary = "Avvia o recupera conversazione",
            description = "Crea una nuova conversazione diretta tra viaggiatore e organizzatore, oppure restituisce quella esistente")
    public ResponseEntity<ConversationResponseDTO> startConversation(
            @Valid @RequestBody ConversationRequestDTO request) {


        var conversation = conversationService.createOrGetConversation(
                request.getTravelerId(),
                request.getOrganizerId()
        );

        return ResponseEntity.ok(conversationMapper.toDto(conversation));
    }

    @GetMapping("/me")
    @Operation(summary = "Le mie conversazioni",
            description = "Recupera tutte le conversazioni dell'utente autenticato (sia come viaggiatore che come organizzatore)")
    public ResponseEntity<List<ConversationResponseDTO>> getMyConversations(
            @AuthenticationPrincipal Jwt jwt) {

        String userId = jwt.getSubject();
        return ResponseEntity.ok(conversationService.getUserConversations(userId));
    }

    @GetMapping("/{conversationId}/messages")
    @Operation(summary = "Storico messaggi",
            description = "Recupera i messaggi paginati di una conversazione, ordinati dal più recente")
    public ResponseEntity<Page<MessageResponseDTO>> getMessages(
            @PathVariable UUID conversationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal Jwt jwt) {

        return ResponseEntity.ok(messageService.getConversationHistory(conversationId, jwt.getSubject(), page, size));
    }

    @PatchMapping("/{conversationId}/read")
    @Operation(summary = "Segna come letti",
            description = "Segna tutti i messaggi non letti di una conversazione come letti per l'utente autenticato")
    public ResponseEntity<Void> markAsRead(
            @PathVariable UUID conversationId,
            @AuthenticationPrincipal Jwt jwt) {

        messageService.markMessagesAsRead(conversationId, jwt.getSubject());
        return ResponseEntity.ok().build();
    }
}