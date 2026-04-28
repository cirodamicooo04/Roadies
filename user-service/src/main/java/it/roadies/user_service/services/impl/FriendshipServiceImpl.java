package it.roadies.user_service.services.impl;

import it.roadies.user_service.data.dto.response.FriendshipResponseDTO;
import it.roadies.user_service.data.dto.response.UserProfileResponseDTO;
import it.roadies.user_service.data.entities.Friendship;
import it.roadies.user_service.data.entities.User;
import it.roadies.user_service.data.entities.enumeration.Status;
import it.roadies.user_service.data.repositories.FriendshipRepository;
import it.roadies.user_service.data.repositories.UserRepository;
import it.roadies.user_service.mappers.FriendshipMapper;
import it.roadies.user_service.mappers.UserMapper;
import it.roadies.user_service.services.FriendshipService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FriendshipServiceImpl implements FriendshipService {
    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final FriendshipMapper friendshipMapper;

    @PreAuthorize("hasRole('TRAVELER') and #senderId == authentication.name")
    @Override
    @Transactional
    public void sendRequest(String senderId, String receiverUsername) {
        User receiver = userRepository.findByUsername(receiverUsername)
                .orElseThrow(() -> new RuntimeException("Utente non trovato"));

        if (senderId.equals(receiver.getKeycloakId()))
            throw new RuntimeException("Non puoi essere amico di te stesso!");

        friendshipRepository.findExistingFriendship(senderId, receiver.getKeycloakId())
                .ifPresent(f -> { throw new RuntimeException("Richiesta già esistente o siete già amici"); });

        Friendship friendship = new Friendship();
        friendship.setRequesterId(userRepository.getReferenceById(senderId));
        friendship.setReceiverId(receiver);
        friendship.setStatus(Status.PENDING);
        friendship.setCreatedAt(LocalDateTime.now());

        friendshipRepository.save(friendship);
    }

    @PreAuthorize("hasRole('TRAVELER') and #currentUserId == authentication.name")
    @Override
    @Transactional
    public void respondToRequest(UUID friendshipId, Status newStatus, String currentUserId) {
        Friendship friendship = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new RuntimeException("Richiesta non trovata"));

        if (!friendship.getReceiverId().getKeycloakId().equals(currentUserId)) {
            throw new RuntimeException("Non autorizzato");
        }

        friendship.setStatus(newStatus);
        friendshipRepository.save(friendship);
    }

    @PreAuthorize("hasRole('TRAVELER') and #userId == authentication.name")
    @Override
    public List<UserProfileResponseDTO> getFriendsList(String userId) {
        List<User> friends = friendshipRepository.findAcceptedFriendsByUser(userId);

        return friends.stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    @PreAuthorize("hasRole('TRAVELER') and #userId == authentication.name")
    @Override
    public List<FriendshipResponseDTO> getDetailedFriendsList(String userId) {
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

    @PreAuthorize("hasRole('TRAVELER') and #userId == authentication.name")
    @Override
    public List<FriendshipResponseDTO> getPendingRequests(String userId) {
        User receiver = userRepository.getReferenceById(userId);
        List<Friendship> pending = friendshipRepository.findByReceiverIdAndStatus(receiver, Status.PENDING);

        return pending.stream().map(f -> {
            FriendshipResponseDTO dto = friendshipMapper.toDto(f);
            dto.setFriendProfile(userMapper.toDto(f.getRequesterId()));
            return dto;
        }).collect(Collectors.toList());
    }

    @PreAuthorize("hasRole('TRAVELER') and #currentUserId == authentication.name")
    @Override
    @Transactional
    public void removeFriend(UUID friendshipId, String currentUserId) {
        Friendship friendship = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new RuntimeException("Relazione di amicizia non trovata"));

        boolean isParticipant = friendship.getRequesterId().getKeycloakId().equals(currentUserId) ||
                friendship.getReceiverId().getKeycloakId().equals(currentUserId);

        if (!isParticipant) {
            throw new RuntimeException("Non sei autorizzato a rimuovere questa amicizia");
        }

        friendshipRepository.delete(friendship);
    }
}