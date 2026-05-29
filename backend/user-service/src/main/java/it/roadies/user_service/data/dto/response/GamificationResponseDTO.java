package it.roadies.user_service.data.dto.response;

import it.roadies.user_service.data.entities.enumeration.Badge;
import lombok.Data;

@Data
public class GamificationResponseDTO {
    private String username;
    private Long points;
    private Badge badge;
    private String avatarUrl;
}
