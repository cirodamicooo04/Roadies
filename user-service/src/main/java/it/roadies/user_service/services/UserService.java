package it.roadies.user_service.services;

import it.roadies.user_service.data.entities.Gamification;
import it.roadies.user_service.data.entities.User;
import it.roadies.user_service.data.entities.enumeration.Badge;
import it.roadies.user_service.data.repositories.GamificationRepository;
import it.roadies.user_service.data.repositories.UserRepository;
import it.roadies.user_service.data.dto.request.UserSyncRequestDTO;
import it.roadies.user_service.data.dto.response.UserProfileResponseDTO;
import it.roadies.user_service.data.dto.result.UserSyncResult;
import it.roadies.user_service.mappers.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final GamificationRepository gamificationRepository;
    private final UserMapper userMapper;

    @Transactional
    public UserSyncResult syncUser(UserSyncRequestDTO requestDto) {

        Optional<User> existingUserOpt = userRepository.findById(requestDto.getKeycloakId());

        // Se l'utente esiste già restituisco un dto e dico che non è un nuovo utente e aggiorno anche il suo ultimo accesso
        if (existingUserOpt.isPresent()) {
            User user = existingUserOpt.get();
            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);

            return new UserSyncResult(userMapper.toDto(user), false);
        }

        // Nel caso in cui ci troviamo davanti ad un nuovo utente lo mappiamo e restitiamo che è un nuovo utente
        User newUser = userMapper.toEntity(requestDto);

        newUser.setCreatedAt(LocalDateTime.now());
        newUser.setLastLogin(LocalDateTime.now());
        newUser.setAvatarUrl("default_avatar.png");

        User savedUser = userRepository.save(newUser);

        //Per ogni nuovo utente mappiamo anche il suo profilo gamification, così che ogni profilo si ritrovi anche un profilo gamification con 0 punti e badge BRONZE

        Gamification userGame = new Gamification();
        userGame.setUser(savedUser);
        userGame.setPoints(0L);
        userGame.setBadge(Badge.BRONZE);
        gamificationRepository.save(userGame);

        savedUser.setGamification(userGame);

        return new UserSyncResult(userMapper.toDto(savedUser), true);
    }

    // Recupera il profilo tramite Keycloak ID
    public UserProfileResponseDTO getProfile(String keycloakId) {
        User user = userRepository.findById(keycloakId)
                .orElseThrow(() -> new RuntimeException("Utente non trovato"));
        return userMapper.toDto(user);
    }

    // Cerca utente per Username (per ricerca amici)
    public UserProfileResponseDTO getProfileByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Username non trovato"));
        return userMapper.toDto(user);
    }

    // Aggiorna il profilo
    @Transactional
    public UserProfileResponseDTO updateProfile(String keycloakId, UserSyncRequestDTO updateDto) {
        User user = userRepository.findById(keycloakId)
                .orElseThrow(() -> new RuntimeException("Utente non trovato"));

        userMapper.updateEntityFromRequest(updateDto, user);
        return userMapper.toDto(userRepository.save(user));
    }

    // Elimina profilo
    @Transactional
    public void deleteProfile(String keycloakId) {
        if (!userRepository.existsById(keycloakId)) {
            throw new RuntimeException("Utente non trovato");
        }

        userRepository.deleteById(keycloakId);
    }
}