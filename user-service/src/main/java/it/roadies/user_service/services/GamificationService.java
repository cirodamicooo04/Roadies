package it.roadies.user_service.services;

import it.roadies.user_service.data.entities.Gamification;
import it.roadies.user_service.data.entities.enumeration.Badge;
import it.roadies.user_service.data.repositories.GamificationRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GamificationService {

    private final GamificationRepository gamificationRepository;

    //Stabiliamo che ogni euro speso si ottengono 10 punti
    private static final int POINTS_PER_EURO = 10;

    @Transactional
    public void addPointsBySpending(String userId, double amountSpent) {
        Gamification gamification = gamificationRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Profilo gamification non trovato"));

        long pointsToAdd = Math.round(amountSpent * POINTS_PER_EURO);

        gamification.setPoints(gamification.getPoints() + pointsToAdd);
        gamification.setBadge(calculateBadge(gamification.getPoints()));

        gamificationRepository.save(gamification);
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