package it.roadies.user_service.services;

import it.roadies.user_service.data.dto.request.UserSyncRequestDTO;
import it.roadies.user_service.data.dto.response.UserProfileResponseDTO;
import it.roadies.user_service.data.dto.result.UserSyncResult;

public interface UserService {
    UserSyncResult syncUser(UserSyncRequestDTO requestDto);
    UserProfileResponseDTO getProfile(String keycloakId);
    UserProfileResponseDTO getProfileByUsername(String username);
    UserProfileResponseDTO updateProfile(String keycloakId, UserSyncRequestDTO updateDto);
    void deleteProfile(String keycloakId);
}