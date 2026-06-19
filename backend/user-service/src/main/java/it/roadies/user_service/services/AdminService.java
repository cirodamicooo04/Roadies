package it.roadies.user_service.services;

public interface AdminService {

    void blockUser(String keycloakId);
    void unblockUser(String keycloakId);
    void demoteOrganizerToUser(String keycloakId);
}
