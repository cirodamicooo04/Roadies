package it.roadies.user_service.services.impl;

import it.roadies.user_service.conf.i8n.MessageLang;
import it.roadies.user_service.data.dto.response.PendingOrganizerRequestResponseDTO;
import it.roadies.user_service.data.dto.response.UserResponseDTO;
import it.roadies.user_service.data.entities.User;
import it.roadies.user_service.data.entities.enumeration.OrganizerRequestStatus;
import it.roadies.user_service.data.repositories.UserRepository;
import it.roadies.user_service.exception.ConflictException;
import it.roadies.user_service.exception.ResourceNotFoundException;
import it.roadies.user_service.mappers.UserMapper;
import it.roadies.user_service.services.AdminService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.RoleRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.UserRepresentation;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserMapper userMapper;
    private final MessageLang messageLang;
    private final UserRepository userRepository;
    private final Keycloak keycloak;

    @Value("${keycloak.realm:roadies-app}")
    private String realmName;

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

    @Override
    @org.springframework.transaction.annotation.Transactional
    public void reviewOrganizerRequest(String targetKeycloakId, boolean approved, String reason) {
        User user = userRepository.findById(targetKeycloakId)
                .orElseThrow(() -> new ResourceNotFoundException(messageLang.getMessage("error.user.notfound")));

        if (user.getOrganizerRequestStatus() != OrganizerRequestStatus.PENDING) {
            throw new ConflictException(messageLang.getMessage("error.request.not.exist"));
        }

        if (approved) {
            user.setOrganizerRequestStatus(OrganizerRequestStatus.ACCEPTED);
            user.setOrganizerRejectionReason(null);

            try {
                UserResource userResource = keycloak.realm(realmName).users().get(targetKeycloakId);

                RoleRepresentation organizerRole = keycloak.realm(realmName).roles().get("ORGANIZER").toRepresentation();

                userResource.roles().realmLevel().add(Collections.singletonList(organizerRole));

                log.info("Ruolo ORGANIZER assegnato con successo su Keycloak all'utente: {}", targetKeycloakId);
            } catch (Exception e) {
                log.error("Impossibile assegnare il ruolo su Keycloak all'utente {}: ", targetKeycloakId, e);
                throw new RuntimeException(messageLang.getMessage("keycloak.error"), e);
            }

        } else {
            if (reason == null || reason.isBlank()) {
                throw new IllegalArgumentException(messageLang.getMessage("error.reason.blank"));
            }
            user.setOrganizerRequestStatus(OrganizerRequestStatus.REJECTED);
            user.setOrganizerRejectionReason(reason);
        }

        user.setOrganizerReviewedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    @Override
    public List<PendingOrganizerRequestResponseDTO> getPendingOrganizerRequests() {
        log.info("Recupero lista utenti con richiesta organizzatore in sospeso");

        List<User> pending = userRepository.findByOrganizerRequestStatus(OrganizerRequestStatus.PENDING);

        if (pending.isEmpty()) {
            log.info("Nessuna richiesta organizzatore in sospeso trovata");
            return List.of();
        }

        return pending.stream()
                .map(userMapper::toPendingDto)
                .toList();
    }
}
