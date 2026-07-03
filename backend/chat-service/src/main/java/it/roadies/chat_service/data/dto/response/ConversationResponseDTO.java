package it.roadies.chat_service.data.dto.response;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConversationResponseDTO {

    private UUID id;
    private String travelerId;
    private String organizerId;
    private UUID travelId;
    private LocalDateTime createdAt;
}
