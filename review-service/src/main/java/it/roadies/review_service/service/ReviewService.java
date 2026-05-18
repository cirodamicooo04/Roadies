package it.roadies.review_service.service;

import it.roadies.review_service.ReviewRepository;
import it.roadies.review_service.data.dto.ReviewRequest;
import it.roadies.review_service.data.dto.ReviewResponse;
import it.roadies.review_service.data.dto.ReviewUpdateRequest;
import it.roadies.review_service.data.entity.Review;
import it.roadies.review_service.data.mapper.ReviewMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewMapper reviewMapper;

    private final ReviewRepository repository;

    public void createReview(ReviewRequest request, String userId) {
        Review review = reviewMapper.toEntity(request, userId);
        repository.save(review);
    }

    public List<ReviewResponse> getByTravel(UUID travelId) {
        return repository.findByTravelId(travelId)
                .stream()
                .map(reviewMapper::toReviewResponse)
                .toList();
    }


    public double getAverageRating(UUID travelId) {
        Double avg = repository.findAverageRatingByTravelId(travelId);
        return avg != null ? avg : 0.0;
    }

    public void deleteReview(UUID reviewId) {
        if (!repository.existsById(reviewId)) {
            throw new RuntimeException("Review does not exist");
        }
        repository.deleteById(reviewId);
    }

    // For administrative purposes
    public List<Review> getAll() {
        return repository.findAll();
    }

    // Update the review by its id
    public Review updateReview(ReviewUpdateRequest reviewUpdateRequest, UUID reviewId) {
        Review existingReview = repository.findById(reviewId).orElseThrow(() -> new RuntimeException("Review not found"));

        existingReview.setRating(reviewUpdateRequest.getRating());
        existingReview.setContent(reviewUpdateRequest.getContent());

        return repository.save(existingReview);
    }

    // Convert a Review entity to a ReviewResponse DTO by its id
    public ReviewResponse createReviewResponse(UUID id) {
        Review review = repository.findById(id).orElseThrow(() -> new RuntimeException("Review not found"));
        return reviewMapper.toReviewResponse(review);
    }
}