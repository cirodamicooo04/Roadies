package it.roadies.travel_service.services.listeners;

import it.roadies.travel_service.data.dao.UserFriendshipRepository;
import it.roadies.travel_service.data.dto.event.FriendshipEvent;
import it.roadies.travel_service.data.entity.UserFriendship;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class FriendshipEventListener {

    private final UserFriendshipRepository userFriendshipRepository;

    @RabbitListener(queues = "travel-service.friendship.accepted.queue")
    @Transactional
    public void handleFriendshipAccepted(FriendshipEvent event) {
        log.info("Ricevuto evento accettazione amicizia tra {} e {}", event.getUserId(), event.getFriendId());

        if (event.getUserId() == null || event.getFriendId() == null) {
            log.error("Evento accettazione scartato: ID nulli nel payload.");
            // Lanciare l'eccezione è meglio di un semplice return per segnalare il fallimento al broker
            throw new IllegalArgumentException("Payload evento non valido: ID nulli");
        }

        try {
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
        } catch (Exception e) {
            log.error("Errore a DB durante l'elaborazione dell'accettazione amicizia: ", e);
            // Fondamentale: rilanciare l'eccezione fa capire a RabbitMQ che il messaggio va ritentato o mandato in DLQ
            throw e;
        }
    }

    @RabbitListener(queues = "travel-service.friendship.deleted.queue")
    @Transactional
    public void handleFriendshipDeleted(FriendshipEvent event) {
        log.info("Ricevuto evento cancellazione amicizia tra {} e {}", event.getUserId(), event.getFriendId());

        if (event.getUserId() == null || event.getFriendId() == null) {
            log.error("Evento cancellazione scartato: ID nulli nel payload.");
            throw new IllegalArgumentException("Payload evento non valido: ID nulli");
        }

        try {
            if ("DELETED".equals(event.getStatus())) {
                userFriendshipRepository.deleteByUserIdAndFriendId(event.getUserId(), event.getFriendId());
                userFriendshipRepository.deleteByUserIdAndFriendId(event.getFriendId(), event.getUserId());
            }
        } catch (Exception e) {
            log.error("Errore a DB durante la cancellazione dell'amicizia: ", e);
            throw e;
        }
    }
}