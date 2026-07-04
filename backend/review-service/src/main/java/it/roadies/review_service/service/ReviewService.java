package it.roadies.review_service.service;

import it.roadies.review_service.data.dto.ReviewRequest;
import it.roadies.review_service.data.dto.ReviewResponse;
import it.roadies.review_service.data.dto.ReviewUpdateRequest;

import java.util.List;
import java.util.UUID;

public interface ReviewService {

    void createReview(ReviewRequest request, UUID travelId, String userId);

    List<ReviewResponse> getByTravel(UUID travelId);

    double getAverageRating(UUID travelId);

    void deleteReview(UUID reviewId, String userId);

    // For administrative purposes
    List<ReviewResponse> getAll();

    // Update the review by its id
    void updateReview(ReviewUpdateRequest reviewUpdateRequest, UUID reviewId, String userId);

    // Convert a Review entity to a ReviewResponse DTO by its id
    ReviewResponse createReviewResponse(UUID id);


}
