package it.roadies.user_service.data.repositories;

import it.roadies.user_service.data.entities.Gamification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GamificationRepository extends JpaRepository<Gamification, String> {

}
