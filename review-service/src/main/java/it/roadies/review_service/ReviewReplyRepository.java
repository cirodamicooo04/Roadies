package it.roadies.review_service;

import it.roadies.review_service.data.entity.ReviewReply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReviewReplyRepository extends JpaRepository<ReviewReply, UUID> {

    Optional<ReviewReply> findByReviewId(UUID reviewId);

    boolean existsByReviewId(UUID reviewId);
}