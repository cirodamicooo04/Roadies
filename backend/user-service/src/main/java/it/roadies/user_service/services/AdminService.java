package it.roadies.user_service.services;

import it.roadies.user_service.data.dto.response.PendingOrganizerRequestResponseDTO;
import it.roadies.user_service.data.dto.response.UserProfileResponseDTO;
import it.roadies.user_service.data.dto.response.UserResponseDTO;

import java.util.List;

public interface AdminService {

    void blockUser(String keycloakId);
    void unblockUser(String keycloakId);
    void reviewOrganizerRequest(String targetKeycloakId, boolean approved, String reason);
    List<PendingOrganizerRequestResponseDTO> getPendingOrganizerRequests();
    List<UserResponseDTO> getUsersByFilter(String filter);
    List<UserProfileResponseDTO> getBest20Travelers();
}
