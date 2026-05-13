package it.roadies.user_service.data.dto.result;

import it.roadies.user_service.data.dto.response.UserProfileResponseDTO;

public record UserSyncResult(UserProfileResponseDTO profile, boolean isNewUser) {
}