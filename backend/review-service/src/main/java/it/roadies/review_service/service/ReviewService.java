package it.roadies.review_service.service;

import it.roadies.review_service.data.dto.ReviewRequest;
import it.roadies.review_service.data.dto.ReviewResponse;
import it.roadies.review_service.data.dto.ReviewUpdateRequest;
import it.roadies.review_service.data.entity.Review;

import java.util.List;
import java.util.UUID;

public interface ReviewService {

    void createReview(ReviewRequest request, UUID travelId, String userId);

    List<ReviewResponse> getByTravel(UUID travelId);

    double getAverageRating(UUID travelId);

    void deleteReview(UUID reviewId, String userId);

    // For administrative purposes
    List<Review> getAll();

    // Update the review by its id
    Review updateReview(ReviewUpdateRequest reviewUpdateRequest, UUID reviewId, String userId);

    // Convert a Review entity to a ReviewResponse DTO by its id
    ReviewResponse createReviewResponse(UUID id);


}
