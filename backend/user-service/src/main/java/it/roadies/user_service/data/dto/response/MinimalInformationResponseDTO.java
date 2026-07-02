package it.roadies.user_service.data.dto.response;

import lombok.Data;

@Data
public class MinimalInformationResponseDTO {
    private String keycloakId;
    private String username;
    private String avatarUrl;
}
