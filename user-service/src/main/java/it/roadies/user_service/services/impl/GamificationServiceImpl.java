package it.roadies.user_service.services.impl;

import it.roadies.user_service.conf.i8n.MessageLang;
import it.roadies.user_service.data.entities.Gamification;
import it.roadies.user_service.data.entities.enumeration.Badge;
import it.roadies.user_service.data.repositories.GamificationRepository;
import it.roadies.user_service.exception.ResourceNotFoundException;
import it.roadies.user_service.services.GamificationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class GamificationServiceImpl implements GamificationService {

    private final GamificationRepository gamificationRepository;
    private final MessageLang messageLang;

    //Stabiliamo che ogni euro speso si ottengono 10 punti
    private static final int POINTS_PER_EURO = 10;

    @PreAuthorize("hasRole('TRAVELER') and #userId == authentication.name")
    @Override
    @Transactional
    public void addPointsBySpending(String userId, double amountSpent) {
        log.info("Iniziato calcolo punti gamification per l'utente ID: {} per una spesa di {}€", userId, amountSpent);

        if (amountSpent <= 0) {
            throw new IllegalArgumentException(messageLang.getMessage("error.points.negative.or.zero"));
        }

        Gamification gamification = gamificationRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("Impossibile aggiornare i punti: profilo gamification non trovato per l'utente ID: {}", userId);
                    return new ResourceNotFoundException(messageLang.getMessage("error.gamification.points"));
                });

        long pointsToAdd = Math.round(amountSpent * POINTS_PER_EURO);
        gamification.setPoints(gamification.getPoints() + pointsToAdd);
        gamification.setBadge(calculateBadge(gamification.getPoints()));

        gamificationRepository.save(gamification);
        log.info("Aggiunti {} punti all'utente ID: {}. Punti totali: {}. Nuovo badge: {}",
                pointsToAdd, userId, gamification.getPoints(), gamification.getBadge());
    }

    private Badge calculateBadge(Long points) {
        if (points >= 250000) return Badge.EMERALD;
        if (points >= 100000) return Badge.DIAMOND;
        if (points >= 50000)  return Badge.PLATINUM;
        if (points >= 10000)  return Badge.GOLD;
        if (points >= 2500)   return Badge.SILVER;
        return Badge.BRONZE;
    }
}