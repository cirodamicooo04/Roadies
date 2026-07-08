package it.roadies.user_service.services.impl;

import it.roadies.shared.i18n.MessageLang;
import it.roadies.user_service.data.dto.request.UserSyncRequestDTO;
import it.roadies.user_service.data.dto.request.UserUpdateRequestDTO;
import it.roadies.user_service.data.dto.response.MinimalInformationResponseDTO;
import it.roadies.user_service.data.dto.response.UserProfileResponseDTO;
import it.roadies.user_service.data.dto.result.UserSyncResult;
import it.roadies.user_service.data.entities.Gamification;
import it.roadies.user_service.data.entities.User;
import it.roadies.user_service.data.entities.enumeration.Badge;
import it.roadies.user_service.data.entities.enumeration.OrganizerRequestStatus;
import it.roadies.user_service.data.repositories.GamificationRepository;
import it.roadies.user_service.data.repositories.UserRepository;
import it.roadies.user_service.exception.ConflictException;
import it.roadies.user_service.exception.ResourceNotFoundException;
import it.roadies.user_service.mappers.UserMapper;
import it.roadies.user_service.services.MinioService;
import it.roadies.user_service.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final String REALM = "roadies-app";

    private final UserRepository userRepository;
    private final GamificationRepository gamificationRepository;
    private final UserMapper userMapper;
    private final MessageLang messageLang;
    private final Keycloak keycloakAdminClient;

    @Value("${minio.avatarBucket}")
    private String avatarBucket;

    private final MinioService minioService;

    @Override
    @Transactional
    public UserSyncResult syncUser(UserSyncRequestDTO requestDto, boolean isOrganizer, boolean isAdmin) {
        log.info("Iniziata sincronizzazione per l'utente con ID: {}", requestDto.getKeycloakId());

        Optional<User> existingUserOpt = userRepository.findById(requestDto.getKeycloakId());

        if (existingUserOpt.isPresent()) {
            User user = existingUserOpt.get();
            user.setLastLogin(LocalDateTime.now());
            
            if (isOrganizer && user.getOrganizerRequestStatus() != OrganizerRequestStatus.ACCEPTED) {
                user.setOrganizerRequestStatus(OrganizerRequestStatus.ACCEPTED);
            }
            
            user.setAdmin(isAdmin);
            
            userRepository.save(user);

            log.info("Utente esistente trovato. Ultimo accesso e ruoli aggiornati.");
            return new UserSyncResult(userMapper.toDto(user), false);
        }

        log.info("Utente non trovato. Creazione di un nuovo profilo in corso...");

        if (requestDto.getUsername() != null &&
                userRepository.findByUsername(requestDto.getUsername()).isPresent()) {
            throw new ConflictException(messageLang.getMessage("error.username.exist"));
        }

        User newUser = userMapper.toEntity(requestDto);
        newUser.setLastLogin(LocalDateTime.now());
        newUser.setAvatarUrl("default_avatar.png");
        
        if (isOrganizer) {
            newUser.setOrganizerRequestStatus(it.roadies.user_service.data.entities.enumeration.OrganizerRequestStatus.ACCEPTED);
        }
        
        newUser.setAdmin(isAdmin);

        User savedUser = userRepository.save(newUser);
        log.info("Nuovo utente creato con successo");

        Gamification userGame = new Gamification();
        userGame.setUser(savedUser);
        userGame.setPoints(0L);
        userGame.setBadge(Badge.BRONZE);
        gamificationRepository.save(userGame);

        log.info("Profilo gamification inizializzato per il nuovo utente");

        savedUser.setGamification(userGame);

        return new UserSyncResult(userMapper.toDto(savedUser), true);
    }

    @Override
    public UserProfileResponseDTO getProfile(String keycloakId) {
        log.info("Recupero profilo per l'utente ID: {}", keycloakId);

        User user = userRepository.findById(keycloakId)
                .orElseThrow(() -> {
                    log.error("Impossibile recuperare il profilo: utente non trovato");
                    return new ResourceNotFoundException(messageLang.getMessage("error.user.notfound"));
                });

        return userMapper.toDto(user);
    }

    @Override
    public List<UserProfileResponseDTO> getProfileByUsername(String username) {
        log.info("Ricerca profilo tramite username: {}", username);

        if (username == null || username.trim().isEmpty()) {
            return Collections.emptyList();
        }

        List<User> foundUsers = userRepository.searchUsersByKeyword(username.trim());

        return foundUsers.stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public UserProfileResponseDTO updateProfile(String keycloakId, UserUpdateRequestDTO updateDto) {
        log.info("Iniziato aggiornamento profilo per l'utente {}", keycloakId);

        User user = userRepository.findById(keycloakId)
                .orElseThrow(() -> {
                    log.error("Impossibile aggiornare il profilo: utente non trovato");
                    return new ResourceNotFoundException(messageLang.getMessage("error.user.notfound"));
                });

        String newFirstName = updateDto.getFirstName() != null
                ? updateDto.getFirstName().trim()
                : user.getFirstName();

        String newLastName = updateDto.getLastName() != null
                ? updateDto.getLastName().trim()
                : user.getLastName();

        updateKeycloakBasicProfile(keycloakId, newFirstName, newLastName);

        if (updateDto.getFirstName() != null) {
            user.setFirstName(newFirstName);
        }

        if (updateDto.getLastName() != null) {
            user.setLastName(newLastName);
        }

        if (updateDto.getBirthDate() != null) {
            user.setBirthDate(updateDto.getBirthDate());
        }

        if (updateDto.getAvatarUrl() != null) {
            user.setAvatarUrl(updateDto.getAvatarUrl().trim());
        }

        User updatedUser = userRepository.save(user);

        log.info("Profilo aggiornato con successo per l'utente {}", keycloakId);
        return userMapper.toDto(updatedUser);
    }

    private void updateKeycloakBasicProfile(String keycloakId, String firstName, String lastName) {
        try {
            UserRepresentation representation =
                    keycloakAdminClient.realm(REALM).users().get(keycloakId).toRepresentation();

            representation.setFirstName(firstName);
            representation.setLastName(lastName);

            keycloakAdminClient.realm(REALM).users().get(keycloakId).update(representation);

            log.info("Aggiornati firstName e lastName su Keycloak per utente {}", keycloakId);

        } catch (jakarta.ws.rs.NotAuthorizedException ex) {
            log.error("Keycloak admin client non autorizzato. Verifica realm, client_id, client_secret e service account roles. userId={}", keycloakId, ex);
            throw new IllegalStateException("Keycloak admin client non autorizzato");

        } catch (jakarta.ws.rs.NotFoundException ex) {
            log.error("Utente non trovato su Keycloak. userId={}", keycloakId, ex);
            throw new ResourceNotFoundException(messageLang.getMessage("error.user.notfound"));

        } catch (Exception ex) {
            log.error("Errore generico durante aggiornamento profilo su Keycloak. userId={}", keycloakId, ex);
            throw new IllegalStateException("Errore tecnico durante aggiornamento del profilo su Keycloak");
        }
    }

    @Override
    @Transactional
    public UserProfileResponseDTO uploadAvatar(String keycloakId, MultipartFile avatarFile) {
        log.info("Upload avatar per utente {}", keycloakId);

        User user = userRepository.findById(keycloakId)
                .orElseThrow(() -> {
                    log.error("Utente non trovato durante upload avatar");
                    return new ResourceNotFoundException(messageLang.getMessage("error.user.notfound"));
                });

        String oldAvatarUrl = user.getAvatarUrl();

        String fileName = minioService.uploadFile(avatarFile, avatarBucket);
        String publicUrl = minioService.getPublicUrl(fileName, avatarBucket);

        user.setAvatarUrl(publicUrl);
        User updatedUser = userRepository.save(user);

        if (oldAvatarUrl != null
                && !oldAvatarUrl.isBlank()
                && !oldAvatarUrl.equals("default_avatar.png")
                && oldAvatarUrl.contains("/")) {
            String oldFileName = oldAvatarUrl.substring(oldAvatarUrl.lastIndexOf("/") + 1);
            minioService.deleteFile(oldFileName, avatarBucket);
        }

        return userMapper.toDto(updatedUser);
    }

    @Override
    @Transactional
    public void requestOrganizerRole(String keycloakId) {
        User user = userRepository.findById(keycloakId)
                .orElseThrow(() -> new ResourceNotFoundException(messageLang.getMessage("error.user.notfound")));

        if (user.getOrganizerRequestStatus() == OrganizerRequestStatus.PENDING) {
            throw new ConflictException(messageLang.getMessage("error.organizer.request.sent"));
        }

        if (user.getOrganizerRequestStatus() == OrganizerRequestStatus.ACCEPTED) {
            throw new ConflictException(messageLang.getMessage("error.organizer.already.approved"));
        }

        user.setOrganizerRequestStatus(OrganizerRequestStatus.PENDING);
        user.setOrganizerRequestedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    @Override
    public List<MinimalInformationResponseDTO> getMinimalInformation(List<String> keycloakId) {
        log.info("Recupero profilo per gli utenti della lista");

        List<User> users = userRepository.findAllById(keycloakId);

        return users.stream()
                .map(userMapper::toMinimalDto)
                .collect(Collectors.toList());
    }

    @Override
    public MinimalInformationResponseDTO getUserMinimalInformation(String username) {
        log.info("Recupero profilo per l'utente username: {}", username);

        Optional<User> user = userRepository.findByUsername(username);
        if (user.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, messageLang.getMessage("error.user.notfound"));
        }

        return userMapper.toMinimalDto(user.get());
    }


    @Override
    public boolean isUserOrganizer(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Utente non trovato con username: " + username));

        return OrganizerRequestStatus.ACCEPTED.equals(user.getOrganizerRequestStatus());
    }

    @Override
    public String findIdByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(user -> user.getKeycloakId().toString()) // O quello che è il tipo del tuo ID
                .orElse(null);
    }
}