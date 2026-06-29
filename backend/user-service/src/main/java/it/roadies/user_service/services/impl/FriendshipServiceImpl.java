package it.roadies.user_service.services.impl;

import it.roadies.user_service.data.dto.response.FriendshipResponseDTO;
import it.roadies.user_service.data.dto.response.UserProfileResponseDTO;
import it.roadies.user_service.data.entities.Friendship;
import it.roadies.user_service.data.entities.User;
import it.roadies.user_service.data.entities.enumeration.Status;
import it.roadies.user_service.data.repositories.FriendshipRepository;
import it.roadies.user_service.data.repositories.UserRepository;
import it.roadies.user_service.exception.ConflictException;
import it.roadies.user_service.exception.ResourceNotFoundException;
import it.roadies.user_service.mappers.FriendshipMapper;
import it.roadies.user_service.mappers.UserMapper;
import it.roadies.user_service.services.FriendshipService;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import it.roadies.user_service.data.dto.event.FriendshipEvent;
import it.roadies.user_service.conf.i8n.MessageLang;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FriendshipServiceImpl implements FriendshipService {
    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final FriendshipMapper friendshipMapper;
    private final MessageLang messageLang;

    private final RabbitTemplate rabbitTemplate;

    @Override
    @Transactional
    public void sendRequest(String senderId, String receiverUsername) {
        log.info("Iniziato invio richiesta di amicizia verso lo username: {}", receiverUsername);

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new ResourceNotFoundException(messageLang.getMessage("error.user.notfound")));

        User receiver = userRepository.findByUsername(receiverUsername)
                .orElseThrow(() -> {
                    log.error("Invio richiesta fallito: utente ricevente non trovato ({})", receiverUsername);
                    return new ResourceNotFoundException(messageLang.getMessage("error.user.notfound"));
                });

        if (senderId.equals(receiver.getKeycloakId())) {
            log.warn("Tentativo di auto-aggiunta amicizia bloccato per l'utente");
            throw new ConflictException(messageLang.getMessage("error.friendship.self"));
        }

        friendshipRepository.findExistingFriendship(senderId, receiver.getKeycloakId())
                .ifPresent(f -> {
                    log.warn("Richiesta di amicizia già esistente");
                    throw new ConflictException(messageLang.getMessage("error.friendship.exists"));
                });

        Friendship friendship = new Friendship();
        friendship.setRequesterId(sender);
        friendship.setReceiverId(receiver);
        friendship.setStatus(Status.PENDING);

        friendshipRepository.save(friendship);
        log.info("Richiesta di amicizia inviata con successo");
    }

    @Override
    @Transactional
    public void respondToRequest(UUID friendshipId, Status newStatus, String currentUserId) {
        log.info("Gestione risposta alla richiesta di amicizia ID: {} con stato: {}", friendshipId, newStatus);

        if (newStatus != Status.ACCEPTED && newStatus != Status.REJECTED) {
            throw new IllegalArgumentException(messageLang.getMessage("error.friendship.status"));
        }

        Friendship friendship = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> {
                    log.error("Risposta fallita: amicizia non trovata (ID: {})", friendshipId);
                    return new ResourceNotFoundException(messageLang.getMessage("error.friendship.request.notfound"));
                });

        if (friendship.getStatus() != Status.PENDING) {
            throw new ConflictException(messageLang.getMessage("error.friendship.already.respond"));
        }

        if (!friendship.getReceiverId().getKeycloakId().equals(currentUserId)) {
            log.error("Tentativo non autorizzato di risposta alla richiesta di amicizia ID: {} da parte dell'utente", friendshipId);
            throw new AccessDeniedException(messageLang.getMessage("error.unauthorized"));
        }

        friendship.setStatus(newStatus);
        friendshipRepository.save(friendship);
        log.info("Stato dell'amicizia ID: {} aggiornato a: {}", friendshipId, newStatus);

        if (newStatus == Status.ACCEPTED) {
            FriendshipEvent event = new FriendshipEvent();
            event.setUserId1(friendship.getRequesterId().getKeycloakId());
            event.setUserId2(friendship.getReceiverId().getKeycloakId());
            event.setStatus("ACCEPTED");

            log.info("Invio evento RabbitMQ 'travel-service.friendship.accepted.queue' per l'amicizia}");
            rabbitTemplate.convertAndSend("user.exchange", "user.friendship.accepted", event);
        }
    }

    @Override
    public List<UserProfileResponseDTO> getFriendsList(String userId) {
        log.info("Recupero lista amici base per l'utente");

        List<Friendship> friendships = friendshipRepository.findAllAcceptedFriendshipsByUser(userId);
        return friendships.stream()
                .map(friendship -> {
                    User friend = friendship.getRequesterId().getKeycloakId().equals(userId)
                            ? friendship.getReceiverId()
                            : friendship.getRequesterId();

                    return userMapper.toDto(friend);
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<FriendshipResponseDTO> getDetailedFriendsList(String userId) {
        log.info("Recupero lista amici dettagliata per l'utente}");
        List<Friendship> friendships = friendshipRepository.findAllAcceptedFriendshipsByUser(userId);

        return friendships.stream().map(f -> {
            FriendshipResponseDTO dto = friendshipMapper.toDto(f);

            User friend = f.getRequesterId().getKeycloakId().equals(userId)
                    ? f.getReceiverId()
                    : f.getRequesterId();

            dto.setFriendProfile(userMapper.toDto(friend));
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public List<FriendshipResponseDTO> getPendingRequests(String userId) {
        log.info("Recupero richieste di amicizia in sospeso per l'utente ID: {}", userId);
        User receiver = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(messageLang.getMessage("error.user.notfound")));
        List<Friendship> pending = friendshipRepository.findByReceiverIdAndStatus(receiver, Status.PENDING);

        return pending.stream().map(f -> {
            FriendshipResponseDTO dto = friendshipMapper.toDto(f);
            dto.setFriendProfile(userMapper.toDto(f.getRequesterId()));
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void removeFriend(UUID friendshipId, String currentUserId) {
        log.info("Richiesta rimozione amicizia ID: {} da parte dell'utente", friendshipId);

        Friendship friendship = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> {
                    log.error("Rimozione fallita: amicizia non trovata (ID: {})", friendshipId);
                    return new ResourceNotFoundException(messageLang.getMessage("error.friendship.notfound"));
                });

        boolean isParticipant = friendship.getRequesterId().getKeycloakId().equals(currentUserId) ||
                friendship.getReceiverId().getKeycloakId().equals(currentUserId);

        if (!isParticipant) {
            log.error("Tentativo non autorizzato di eliminazione dell'amicizia ID: {} da parte dell'utente", friendshipId);
            throw new AccessDeniedException(messageLang.getMessage("error.unauthorized"));
        }

        friendshipRepository.delete(friendship);
        log.info("Amicizia ID: {} eliminata con successo dal DB", friendshipId);

        FriendshipEvent event = new FriendshipEvent();
        event.setUserId1(friendship.getRequesterId().getKeycloakId());
        event.setUserId2(friendship.getReceiverId().getKeycloakId());
        event.setStatus("DELETED");

        log.info("Invio evento RabbitMQ 'user.exchange' per l'amicizia rimossa");
        rabbitTemplate.convertAndSend("user.exchange", "user.friendship.deleted", event);
    }

    @Override
    public List<FriendshipResponseDTO> getSentRequests(String userId) {
        log.info("Recupero richieste di amicizia inviate in sospeso per l'utente ID: {}", userId);
        User requester = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(messageLang.getMessage("error.user.notfound")));

        List<Friendship> sent = friendshipRepository.findByRequesterIdAndStatus(requester, Status.PENDING);

        return sent.stream().map(f -> {
            FriendshipResponseDTO dto = friendshipMapper.toDto(f);
            dto.setFriendProfile(userMapper.toDto(f.getReceiverId()));
            return dto;
        }).collect(Collectors.toList());
    }

}