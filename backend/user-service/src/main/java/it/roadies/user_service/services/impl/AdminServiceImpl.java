package it.roadies.user_service.services.impl;

import it.roadies.user_service.data.entities.User;
import it.roadies.user_service.data.repositories.UserRepository;
import it.roadies.user_service.services.AdminService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
//import org.keycloak.admin.client.Keycloak;
//import org.keycloak.representations.idm.UserRepresentation;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    //private final Keycloak keycloak;

    @Override
    @Transactional
    public void blockUser(String keycloakId) {
        log.info("Admin operation: Request to block user with Keycloak ID: {}", keycloakId);

        User user = userRepository.findById(keycloakId)
                .orElseThrow(() -> new RuntimeException("User not found with Keycloak ID: " + keycloakId));

        //user.setEnabled(false);
        userRepository.save(user);
        log.info("User with Keycloak ID: {} has been blocked.", keycloakId);
    }

    @Override
    @Transactional
    public void unblockUser(String keycloakId) {
        log.info("Admin operation: Request to unblock user with Keycloak ID: {}", keycloakId);

        User user = userRepository.findById(keycloakId)
                .orElseThrow(() -> new RuntimeException("User not found with Keycloak ID: " + keycloakId));

        //user.setEnabled(true);
        userRepository.save(user);
        log.info("User with Keycloak ID: {} has been unblocked.", keycloakId);
    }

    @Override
    @Transactional
    public void demoteOrganizerToUser(String keycloakId) {
        log.info("Admin operation: Request to demote organizer to user with Keycloak ID: {}", keycloakId);

        User user = userRepository.findById(keycloakId)
                .orElseThrow(() -> new RuntimeException("User not found with Keycloak ID: " + keycloakId));

        //user.setRole(User.Role.USER);
        userRepository.save(user);
        log.info("User with Keycloak ID: {} has been demoted from organizer to user.", keycloakId);
    }
}
