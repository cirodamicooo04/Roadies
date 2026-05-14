package it.roadies.travel_service.data.dto.event;

import lombok.Data;
import java.util.UUID;

@Data
public class FriendshipEvent {
    private String userId;
    private String friendId;
    private String status;
}