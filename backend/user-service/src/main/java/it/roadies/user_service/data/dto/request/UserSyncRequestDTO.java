package it.roadies.user_service.data.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.time.LocalDate;

@Data
public class UserSyncRequestDTO {
    private String keycloakId;

    @Email
    @Size(max=255)
    private String email;
    @Size(min=3, max=30)
    private String username;
    @Size(min=1, max=30)
    private String firstName;
    @Size(min=1, max=30)
    private String lastName;
    @Past
    private LocalDate birthDate;
}