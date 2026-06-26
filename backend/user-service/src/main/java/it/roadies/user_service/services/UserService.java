package it.roadies.user_service.services;

import it.roadies.user_service.data.dto.request.UserSyncRequestDTO;
import it.roadies.user_service.data.dto.request.UserUpdateRequestDTO;
import it.roadies.user_service.data.dto.response.PendingOrganizerRequestResponseDTO;
import it.roadies.user_service.data.dto.response.UserProfileResponseDTO;
import it.roadies.user_service.data.dto.result.UserSyncResult;

import java.util.List;

public interface UserService {
    UserSyncResult syncUser(UserSyncRequestDTO requestDto);
    UserProfileResponseDTO getProfile(String keycloakId);
    UserProfileResponseDTO getProfileByUsername(String username);
    UserProfileResponseDTO updateProfile(String keycloakId, UserUpdateRequestDTO updateDto);
    void deleteProfile(String keycloakId);
    void requestOrganizerRole(String keycloakId);
    void reviewOrganizerRequest(String targetKeycloakId, boolean approved, String reason);
    List<PendingOrganizerRequestResponseDTO> getPendingOrganizerRequests();
}