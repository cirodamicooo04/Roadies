package it.roadies.user_service.services.listeners;

import it.roadies.shared.i18n.MessageLang;
import it.roadies.shared.contracts.GamificationEvent;
import it.roadies.user_service.services.GamificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GamificationListener {
    private final GamificationService gamificationService;

    @RabbitListener(queues = "gamification-queue-add")
    public void addGamificationPoints(GamificationEvent gamificationEvent) {
        gamificationService.addPointsBySpending(gamificationEvent.getUserId(), gamificationEvent.getPrice());
    }

    @RabbitListener(queues = "gamification-queue-remove")
    public void removePoints(GamificationEvent gamificationEvent) {
        gamificationService.removePoints(gamificationEvent.getUserId(), gamificationEvent.getPrice());
    }



}
