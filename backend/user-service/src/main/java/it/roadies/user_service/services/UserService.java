package it.roadies.user_service.services;

import it.roadies.user_service.data.dto.request.UserSyncRequestDTO;
import it.roadies.user_service.data.dto.request.UserUpdateRequestDTO;
import it.roadies.user_service.data.dto.response.MinimalInformationResponseDTO;
import it.roadies.user_service.data.dto.response.PendingOrganizerRequestResponseDTO;
import it.roadies.user_service.data.dto.response.UserProfileResponseDTO;
import it.roadies.user_service.data.dto.result.UserSyncResult;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UserService {
    UserSyncResult syncUser(UserSyncRequestDTO requestDto);
    UserProfileResponseDTO getProfile(String keycloakId);
    List<UserProfileResponseDTO> getProfileByUsername(String username);
    UserProfileResponseDTO updateProfile(String keycloakId, UserUpdateRequestDTO updateDto);
    UserProfileResponseDTO uploadAvatar(String keycloakId, MultipartFile avatarFile);
    void requestOrganizerRole(String keycloakId);
    List<MinimalInformationResponseDTO> getMinimalInformation(List<String> keycloakId);
}