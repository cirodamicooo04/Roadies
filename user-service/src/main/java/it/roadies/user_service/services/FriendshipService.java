package it.roadies.user_service.services;

import it.roadies.user_service.data.dto.response.UserProfileResponseDTO;
import it.roadies.user_service.data.entities.Friendship;
import it.roadies.user_service.data.entities.User;
import it.roadies.user_service.data.entities.enumeration.Status;
import it.roadies.user_service.data.repositories.FriendshipRepository;
import it.roadies.user_service.data.repositories.UserRepository;
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
}