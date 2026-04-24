package it.roadies.user_service.data.dto.response;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class FriendshipResponseDTO {
    private UUID id;
    private String status;
    private LocalDateTime createdAt;
    private UserProfileResponseDTO friendProfile;
}