package it.roadies.shared.contracts;

import lombok.Data;

@Data
public class FriendshipEvent {
    private String userId;
    private String friendId;
    private String status;
}