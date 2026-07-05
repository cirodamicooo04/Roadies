package it.roadies.user_service.services.impl;

import it.roadies.user_service.conf.i8n.MessageLang;
import it.roadies.user_service.data.dto.response.PendingOrganizerRequestResponseDTO;
import it.roadies.user_service.data.dto.response.UserProfileResponseDTO;
import it.roadies.user_service.data.dto.response.UserResponseDTO;
import it.roadies.user_service.data.entities.Gamification;
import it.roadies.user_service.data.entities.User;
import it.roadies.user_service.data.entities.enumeration.OrganizerRequestStatus;
import it.roadies.user_service.data.repositories.GamificationRepository;
import it.roadies.user_service.data.repositories.UserRepository;
import it.roadies.user_service.exception.ConflictException;
import it.roadies.user_service.exception.ResourceNotFoundException;
import it.roadies.user_service.mappers.AdminUserMapper;
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
    private final AdminUserMapper adminUserMapper;
    private final GamificationRepository gamificationRepository;
    private final UserRepository userRepository;
    private final MessageLang messageLang;
    private final Keycloak keycloak;

    private boolean isUserEnabledInKeycloak(String keycloakId) {
        return keycloak.realm(realmName).users().get(keycloakId).toRepresentation().isEnabled();
    }

    @Value("${keycloak.realm:roadies-app}")
    private String realmName;

    @Override
    @Transactional
    public void blockUser(String keycloakId) {
        log.info("Admin: Blocking user: {}", keycloakId);

        var userResource = keycloak.realm(realmName).users().get(keycloakId);
        UserRepresentation userRep = userResource.toRepresentation();
        userRep.setEnabled(false);
        userResource.update(userRep);

        log.info("User {} blocked in Keycloak", keycloakId);
    }

    @Override
    @Transactional
    public void unblockUser(String keycloakId) {
        log.info("Admin: Unblocking user: {}", keycloakId);

        var userResource = keycloak.realm(realmName).users().get(keycloakId);
        UserRepresentation userRep = userResource.toRepresentation();
        userRep.setEnabled(true);
        userResource.update(userRep);

        log.info("User {} unblocked in Keycloak", keycloakId);
    }

    @Override
    @Transactional
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

    @Override
    @Transactional
    public List<UserResponseDTO> getUsersByFilter(String filter) {
        List<User> users;

        switch (filter.toUpperCase()) {
            case "ORGANIZERS" -> users = userRepository.findByOrganizerRequestStatus(OrganizerRequestStatus.ACCEPTED);
            case "BANNED", "ACTIVE" -> users = userRepository.findAll();
            default -> users = userRepository.findAll();
        }

        return users.stream()
                .map(u -> {
                    UserResponseDTO dto = adminUserMapper.toDto(u);
                    dto.setEnabled(isUserEnabledInKeycloak(u.getKeycloakId()));
                    return dto;
                })
                .filter(dto -> {
                    if ("BANNED".equals(filter.toUpperCase())) return !dto.isEnabled();
                    if ("ACTIVE".equals(filter.toUpperCase())) return dto.isEnabled();
                    return true;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<UserProfileResponseDTO> getBest20Travelers(){
        log.info("Recupero top 20 utenti con più punti");

        List<Gamification> topGamification = gamificationRepository.findTop20ByOrderByPointsDesc();

        return topGamification.stream()
                .map(gamification -> userMapper.toDto(gamification.getUser()))
                .collect(Collectors.toList());
    }
}
