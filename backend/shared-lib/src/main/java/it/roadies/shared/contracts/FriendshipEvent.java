package it.roadies.shared.contracts;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class FriendshipEvent {
    @JsonProperty("userId1")
    private String userId;

    @JsonProperty("userId2")
    private String friendId;

    private String status;
}