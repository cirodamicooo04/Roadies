package it.roadies.chat_service.data.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageRequestDTO {

    @NotNull(message = "L'ID della conversazione è obbligatorio")
    private UUID conversationId;

    @NotBlank(message = "Il contenuto del messaggio non può essere vuoto")
    private String content;
}
