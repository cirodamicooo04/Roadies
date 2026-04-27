package it.roadies.review_service;

import it.roadies.review_service.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface ReviewRepository extends JpaRepository<Review, UUID> {

    List<Review> findByTravelId(UUID travelId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.travelId = :travelId")
    Double findAverageRatingByTravelId(UUID travelId);
}