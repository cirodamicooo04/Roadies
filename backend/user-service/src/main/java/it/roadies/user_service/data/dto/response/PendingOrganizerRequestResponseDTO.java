package it.roadies.user_service.data.dto.response;

import it.roadies.user_service.data.entities.enumeration.OrganizerRequestStatus;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PendingOrganizerRequestResponseDTO {
    private String keycloakId;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private OrganizerRequestStatus organizerRequestStatus;
    private LocalDateTime organizerRequestedAt;
}