package it.roadies.user_service.data.dto.event;
import lombok.Data;

@Data
public class FriendshipEvent {
    private String userId1;
    private String userId2;
    private String status;
}
