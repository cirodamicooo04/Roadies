package it.roadies.chat_service.conf;

import it.roadies.chat_service.data.entities.Conversation;
import it.roadies.chat_service.data.repositories.ConversationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AuthChannelInterceptor implements ChannelInterceptor {

    private final JwtDecoder jwtDecoder;
    private final ConversationRepository conversationRepository;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null) {
            if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                String authHeader = accessor.getFirstNativeHeader("Authorization");

                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    try {
                        String token = authHeader.substring(7);
                        Jwt jwt = jwtDecoder.decode(token);
                        JwtAuthenticationToken authentication = new JwtAuthenticationToken(jwt);
                        accessor.setUser(authentication);
                    } catch (Exception e) {
                        throw new IllegalArgumentException("Token JWT non valido o scaduto");
                    }
                }
            } else if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
                String destination = accessor.getDestination();
                if (destination != null && destination.startsWith("/topic/conversation/")) {
                    String conversationIdStr = destination.substring("/topic/conversation/".length());
                    UUID conversationId;
                    try {
                        conversationId = UUID.fromString(conversationIdStr);
                    } catch (IllegalArgumentException e) {
                        throw new IllegalArgumentException("ID conversazione non valido");
                    }

                    if (accessor.getUser() == null || !(accessor.getUser() instanceof JwtAuthenticationToken)) {
                        throw new IllegalArgumentException("Utente non autenticato per l'iscrizione");
                    }

                    JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) accessor.getUser();
                    String userId = jwtAuth.getToken().getSubject();

                    Conversation conversation = conversationRepository.findById(conversationId)
                            .orElseThrow(() -> new IllegalArgumentException("Conversazione non trovata"));

                    if (!userId.equals(conversation.getTravelerId()) && !userId.equals(conversation.getOrganizerId())) {
                        throw new IllegalArgumentException("Utente non autorizzato ad accedere a questa conversazione");
                    }
                }
            }
        }

        return message;
    }
}
