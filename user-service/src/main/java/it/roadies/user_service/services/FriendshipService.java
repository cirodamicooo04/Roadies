package it.roadies.user_service.services;

import it.roadies.user_service.data.dto.response.FriendshipResponseDTO;
import it.roadies.user_service.data.dto.response.UserProfileResponseDTO;
import it.roadies.user_service.data.entities.Friendship;
import it.roadies.user_service.data.entities.User;
import it.roadies.user_service.data.entities.enumeration.Status;
import it.roadies.user_service.data.repositories.FriendshipRepository;
import it.roadies.user_service.data.repositories.UserRepository;
import it.roadies.user_service.mappers.FriendshipMapper;
import it.roadies.user_service.mappers.UserMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FriendshipService {
    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final FriendshipMapper friendshipMapper;

    @Transactional
    public void sendRequest(String senderId, String receiverUsername) {
        User receiver = userRepository.findByUsername(receiverUsername)
                .orElseThrow(() -> new RuntimeException("Utente non trovato"));

        if (senderId.equals(receiver.getKeycloak_id()))
            throw new RuntimeException("Non puoi essere amico di te stesso!");

        friendshipRepository.findExistingFriendship(senderId, receiver.getKeycloak_id())
                .ifPresent(f -> { throw new RuntimeException("Richiesta già esistente o siete già amici"); });

        Friendship friendship = new Friendship();
        friendship.setRequester_id(userRepository.getReferenceById(senderId));
        friendship.setReceiver_id(receiver);
        friendship.setStatus(Status.PENDING);
        friendship.setCreated_at(LocalDateTime.now());

        friendshipRepository.save(friendship);
    }

    @Transactional
    public void respondToRequest(UUID friendshipId, Status newStatus, String currentUserId) {
        Friendship friendship = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new RuntimeException("Richiesta non trovata"));

        if (!friendship.getReceiver_id().getKeycloak_id().equals(currentUserId)) {
            throw new RuntimeException("Non autorizzato");
        }

        friendship.setStatus(newStatus);
        friendshipRepository.save(friendship);
    }

    public List<UserProfileResponseDTO> getFriendsList(String userId) {

        List<User> friends = friendshipRepository.findAcceptedFriendsByUser(userId);

        return friends.stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<FriendshipResponseDTO> getDetailedFriendsList(String userId) {
        List<Friendship> friendships = friendshipRepository.findAllAcceptedFriendshipsByUser(userId);

        return friendships.stream().map(f -> {
            FriendshipResponseDTO dto = friendshipMapper.toDto(f);

            User friend = f.getRequester_id().getKeycloak_id().equals(userId)
                    ? f.getReceiver_id()
                    : f.getRequester_id();

            dto.setFriendProfile(userMapper.toDto(friend));
            return dto;
        }).collect(Collectors.toList());
    }

    public List<FriendshipResponseDTO> getPendingRequests(String userId) {
        User receiver = userRepository.getReferenceById(userId);
        List<Friendship> pending = friendshipRepository.findByReceiver_idAndStatus(receiver, Status.PENDING);

        return pending.stream().map(f -> {
            FriendshipResponseDTO dto = friendshipMapper.toDto(f);
            dto.setFriendProfile(userMapper.toDto(f.getRequester_id()));
            return dto;
        }).collect(Collectors.toList());
    }

    @Transactional
    public void removeFriend(UUID friendshipId, String currentUserId) {
        Friendship friendship = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new RuntimeException("Relazione di amicizia non trovata"));


        boolean isParticipant = friendship.getRequester_id().getKeycloak_id().equals(currentUserId) ||
                friendship.getReceiver_id().getKeycloak_id().equals(currentUserId);

        if (!isParticipant) {
            throw new RuntimeException("Non sei autorizzato a rimuovere questa amicizia");
        }

        friendshipRepository.delete(friendship);
    }
}