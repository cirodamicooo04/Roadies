package it.roadies.user_service.data.dto.request;

import lombok.Data;
import java.time.LocalDate;

@Data
public class UserSyncRequestDTO {
    private String keycloak_id;
    private String email;
    private String username;
    private String first_name;
    private String last_name;
    private LocalDate birth_date;
}