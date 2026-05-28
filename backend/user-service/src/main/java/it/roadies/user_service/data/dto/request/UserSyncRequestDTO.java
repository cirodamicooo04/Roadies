package it.roadies.user_service.data.dto.request;

import lombok.Data;
import java.time.LocalDate;

@Data
public class UserSyncRequestDTO {
    private String keycloakId;
    private String email;
    private String username;
    private String firstName;
    private String lastName;
    private LocalDate birthDate;
}