package it.roadies.chat_service.data.dto.response;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageResponseDTO {

    private UUID id;
    private UUID conversationId;
    private String senderId;
    private String senderName;
    private String content;
    private LocalDateTime timestamp;
    private boolean read;
}
