package it.roadies.user_service.data.dto.response;

import it.roadies.user_service.data.entities.enumeration.Badge;
import lombok.Data;

@Data
public class UserResponseDTO {
    private String keycloakId;
    private String username;
    private String firstName;
    private String lastName;
    private String avatarUrl;

    private Long points;
    private Badge badge;
}