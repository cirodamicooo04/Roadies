package it.roadies.user_service.services;

import it.roadies.user_service.data.dto.response.FriendshipResponseDTO;
import it.roadies.user_service.data.dto.response.UserProfileResponseDTO;
import it.roadies.user_service.data.entities.enumeration.Status;

import java.util.List;
import java.util.UUID;

public interface FriendshipService {
    void sendRequest(String senderId, String receiverUsername);
    void respondToRequest(UUID friendshipId, Status newStatus, String currentUserId);
    List<UserProfileResponseDTO> getFriendsList(String userId);
    List<FriendshipResponseDTO> getDetailedFriendsList(String userId);
    List<FriendshipResponseDTO> getPendingRequests(String userId);
    void removeFriend(String friendUsername, String currentUserId);
    List<FriendshipResponseDTO> getSentRequests(String userId);
}