package it.roadies.user_service.services.impl;

import it.roadies.user_service.conf.i8n.MessageLang;
import it.roadies.user_service.data.entities.Gamification;
import it.roadies.user_service.data.entities.User;
import it.roadies.user_service.data.entities.enumeration.Badge;
import it.roadies.user_service.data.entities.enumeration.OrganizerRequestStatus;
import it.roadies.user_service.data.repositories.GamificationRepository;
import it.roadies.user_service.data.repositories.UserRepository;
import it.roadies.user_service.data.dto.request.UserSyncRequestDTO;
import it.roadies.user_service.data.dto.response.UserProfileResponseDTO;
import it.roadies.user_service.data.dto.result.UserSyncResult;
import it.roadies.user_service.exception.ConflictException;
import it.roadies.user_service.exception.ResourceNotFoundException;
import it.roadies.user_service.mappers.UserMapper;
import it.roadies.user_service.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final GamificationRepository gamificationRepository;
    private final UserMapper userMapper;
    private final MessageLang messageLang;

    @Override
    @Transactional
    //@PreAuthorize("isAuthenticated() and #requestDto.keycloakId == authentication.name")
    public UserSyncResult syncUser(UserSyncRequestDTO requestDto) {
        log.info("Iniziata sincronizzazione per l'utente con ID: {}", requestDto.getKeycloakId());

        if (requestDto.getUsername() != null &&
                userRepository.findByUsername(requestDto.getUsername()).isPresent()) {
            throw new ConflictException(messageLang.getMessage("error.username.exist"));
        }

        Optional<User> existingUserOpt = userRepository.findById(requestDto.getKeycloakId());

        // Se l'utente esiste già restituisco un dto e dico che non è un nuovo utente e aggiorno anche il suo ultimo accesso
        if (existingUserOpt.isPresent()) {
            User user = existingUserOpt.get();
            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);

            log.info("Utente esistente trovato. Ultimo accesso aggiornato.");
            return new UserSyncResult(userMapper.toDto(user), false);
        }

        log.info("Utente non trovato. Creazione di un nuovo profilo in corso...");

        // Nel caso in cui ci troviamo davanti ad un nuovo utente lo mappiamo e restitiamo che è un nuovo utente
        User newUser = userMapper.toEntity(requestDto);

        newUser.setLastLogin(LocalDateTime.now());
        newUser.setAvatarUrl("default_avatar.png");

        User savedUser = userRepository.save(newUser);
        log.info("Nuovo utente creato con successo");

        //Per ogni nuovo utente mappiamo anche il suo profilo gamification, così che ogni profilo si ritrovi anche un profilo gamification con 0 punti e badge BRONZE
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
    //@PreAuthorize("isAuthenticated() and #keycloakId == authentication.name")
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
    //@PreAuthorize("isAuthenticated()")
    public UserProfileResponseDTO getProfileByUsername(String username) {
        log.info("Ricerca profilo tramite username: {}", username);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.error("Impossibile recuperare il profilo: username non trovato ({})", username);
                    return new ResourceNotFoundException(messageLang.getMessage("error.username.notfound"));
                });
        return userMapper.toDto(user);
    }

    @Override
    @Transactional
    //@PreAuthorize("isAuthenticated() and #keycloakId == authentication.name")
    public UserProfileResponseDTO updateProfile(String keycloakId, UserSyncRequestDTO updateDto) {
        log.info("Iniziato aggiornamento profilo per l'utente");
        User user = userRepository.findById(keycloakId)
                .orElseThrow(() -> {
                    log.error("Impossibile aggiornare il profilo: utente non trovato");
                    return new ResourceNotFoundException(messageLang.getMessage("error.user.notfound"));
                });

        if (updateDto.getUsername() != null && !updateDto.getUsername().equals(user.getUsername())) {
            if (userRepository.findByUsername(updateDto.getUsername()).isPresent()) {
                throw new ConflictException(messageLang.getMessage("error.user.username.exists"));
            }
        }

        userMapper.updateEntityFromRequest(updateDto, user);
        User updatedUser = userRepository.save(user);

        log.info("Profilo aggiornato con successo per l'utente");
        return userMapper.toDto(updatedUser);
    }

    @Override
    @Transactional
    //@PreAuthorize("isAuthenticated() and #keycloakId == authentication.name")
    public void deleteProfile(String keycloakId) {
        log.info("Richiesta di eliminazione profilo per l'utente");
        if (!userRepository.existsById(keycloakId)) {
            log.error("Impossibile eliminare il profilo: utente non trovato");
            throw new ResourceNotFoundException(messageLang.getMessage("error.user.notfound"));
        }

        userRepository.deleteById(keycloakId);
        log.info("Profilo eliminato con successo per l'utente}");
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


            //TODO: FARE IL CAMBIO RUOLO SU KEYCLOACK



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
    public List<UserProfileResponseDTO> getPendingOrganizerRequests() {
        log.info("Recupero lista utenti con richiesta organizzatore in sospeso");

        List<User> pending = userRepository.findByOrganizerRequestStatus(OrganizerRequestStatus.PENDING);

        if (pending.isEmpty()) {
            log.info("Nessuna richiesta organizzatore in sospeso trovata");
            return List.of();
        }

        return pending.stream()
                .map(userMapper::toDto)
                .toList();
    }
}