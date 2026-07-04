package it.roadies.review_service.data.dao;

import it.roadies.review_service.data.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import it.roadies.review_service.data.dto.RatingSummary;

import java.util.List;
import java.util.UUID;

public interface ReviewRepository extends JpaRepository<Review, UUID> {

    List<Review> findByTravelId(UUID travelId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.travelId = :travelId")
    Double findAverageRatingByTravelId(UUID travelId);

    @Query("SELECT COUNT(r) as totalRatings, AVG(r.rating) as averageRating FROM Review r WHERE r.travelId = :travelId")
    RatingSummary findRatingSummaryByTravelId(@Param("travelId") UUID travelId);

    // Per scopi di debug o amministrativi, potresti voler recuperare tutte le recensioni
    List<Review> findAll();

    // Implementazione personalizzata per verificare se l'utente ha già recensito questo viaggio
    boolean existsByTravelIdAndUserId(UUID travelId, String userId);
}