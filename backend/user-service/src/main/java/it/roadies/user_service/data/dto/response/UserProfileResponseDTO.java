package it.roadies.user_service.data.dto.response;

import it.roadies.user_service.data.entities.enumeration.Badge;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UserProfileResponseDTO {
    private String username;
    private String firstName;
    private String lastName;
    private String avatarUrl;

    //Questi dati li prendiamo dalla Gamification per avere un "riassunto" completo per il profilo del nostro utente
    private Long points;
    private Badge badge;
}