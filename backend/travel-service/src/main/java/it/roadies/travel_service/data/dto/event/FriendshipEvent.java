package it.roadies.travel_service.data.dto.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.UUID;

@Data
public class FriendshipEvent {
    @JsonProperty("userId1")
    private String userId;

    @JsonProperty("userId2")
    private String friendId;

    private String status;
}