// File: UserProfileResponseDTO.java
package it.roadies.user_service.data.dto.response;

import it.roadies.user_service.data.entities.enumeration.Badge;
import lombok.Data;

@Data
public class UserProfileResponseDTO {
    private String username;
    private String first_name;
    private String last_name;
    private String avatar_url;

    //Questi dati li prendiamo dalla Gamification per avere un "riassunto" completo per il profilo del nostro utente
    private Long points;
    private Badge badge;
}