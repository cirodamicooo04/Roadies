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
public class ConversationRequestDTO {

    @NotBlank(message = "L'ID del viaggiatore è obbligatorio")
    private String travelerId;

    @NotBlank(message = "L'ID dell'organizzatore è obbligatorio")
    private String organizerId;
}
