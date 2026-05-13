package it.roadies.travel_service.listeners;

import it.roadies.travel_service.data.dao.UserFriendshipRepository;
import it.roadies.travel_service.data.dto.event.FriendshipEvent;
import it.roadies.travel_service.data.entity.UserFriendship;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class FriendshipEventListener {

    private final UserFriendshipRepository userFriendshipRepository;

    @RabbitListener(queues = "travel-service.friendship.accepted.queue")
    @Transactional
    public void handleFriendshipAccepted(FriendshipEvent event) {
        if ("ACCEPTED".equals(event.getStatus())) {
            UserFriendship f1 = new UserFriendship();
            f1.setUserId(event.getUserId());
            f1.setFriendId(event.getFriendId());

            UserFriendship f2 = new UserFriendship();
            f2.setUserId(event.getFriendId());
            f2.setFriendId(event.getUserId());

            userFriendshipRepository.save(f1);
            userFriendshipRepository.save(f2);
        }
    }

    @RabbitListener(queues = "travel-service.friendship.deleted.queue")
    @Transactional
    public void handleFriendshipDeleted(FriendshipEvent event) {
        if ("DELETED".equals(event.getStatus())) {
            userFriendshipRepository.deleteByUserIdAndFriendId(event.getUserId(), event.getFriendId());
            userFriendshipRepository.deleteByUserIdAndFriendId(event.getFriendId(), event.getUserId());
        }
    }
}