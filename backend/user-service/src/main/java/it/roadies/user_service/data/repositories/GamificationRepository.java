package it.roadies.user_service.data.repositories;

import it.roadies.user_service.data.entities.Gamification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface GamificationRepository extends JpaRepository<Gamification, String> {
    @Query("SELECT g FROM Gamification g ORDER BY g.points DESC LIMIT 20")
    List<Gamification> findTop20ByOrderByPointsDesc();
}
