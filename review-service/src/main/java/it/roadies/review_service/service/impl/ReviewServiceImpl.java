package it.roadies.review_service.service.impl;

import it.roadies.review_service.conf.i8n.MessageLang;
import it.roadies.review_service.data.dao.ReviewRepository;
import it.roadies.review_service.data.dto.ReviewRequest;
import it.roadies.review_service.data.dto.ReviewResponse;
import it.roadies.review_service.data.dto.ReviewUpdateRequest;
import it.roadies.review_service.data.entity.Review;
import it.roadies.review_service.data.mapper.ReviewMapper;
import it.roadies.review_service.exceptions.ReviewNotFoundException;
import it.roadies.review_service.exceptions.AccessDeniedException;
import it.roadies.review_service.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewMapper reviewMapper;
    private final MessageLang messageLang;
    private final ReviewRepository repository;

    @Override
    public void createReview(ReviewRequest request, UUID travelId, String userId) {
        Review review = reviewMapper.toEntity(request, userId);
        review.setTravelId(travelId);
        // Check if the user has already reviewed this travel
        if (repository.existsByTravelIdAndUserId(travelId, userId)) {
            throw new AccessDeniedException("l'utente" + userId + "ha tentato di accedere ad una risorsa non autorizzato");
        }
        repository.save(review);
    }

    @Override
    public List<ReviewResponse> getByTravel(UUID travelId) {
        return repository.findByTravelId(travelId)
                .stream()
                .map(reviewMapper::toReviewResponse)
                .toList();
    }

    @Override
    public double getAverageRating(UUID travelId) {
        Double avg = repository.findAverageRatingByTravelId(travelId);
        return avg;
    }

    public void deleteReview(UUID reviewId, String userId) {
        if (!repository.existsById(reviewId)) {
            throw new ReviewNotFoundException(messageLang.getMessage("review.not.exists"));
        }
        if (!repository.findById(reviewId).get().getUserId().equals(userId)) {
            throw new AccessDeniedException("l'utente" + userId + "ha tentato di accedere ad una risorsa non autorizzato");
        }
        repository.deleteById(reviewId);
    }

    // For administrative purposes
    @Override
    public List<Review> getAll() {
        return repository.findAll();
    }

    // Update the review by its id
    @Override
    public Review updateReview(ReviewUpdateRequest reviewUpdateRequest, UUID reviewId, String userId) {
        Review existingReview = repository.findById(reviewId).orElseThrow(() -> new ReviewNotFoundException(messageLang.getMessage("review.not.found")));
        // Only the user who created the review can update it
        if (!existingReview.getUserId().equals(userId)) {
            throw new AccessDeniedException("l'utente" + userId + "ha tentato di accedere ad una risorsa non autorizzato");
        }
        existingReview.setRating(reviewUpdateRequest.getRating());
        existingReview.setContent(reviewUpdateRequest.getContent());

        return repository.save(existingReview);
    }

    // Convert a Review entity to a ReviewResponse DTO by its id
    @Override
    public ReviewResponse createReviewResponse(UUID id) {
        Review review = repository.findById(id).orElseThrow(() -> new ReviewNotFoundException(messageLang.getMessage("review.not.found")));
        return reviewMapper.toReviewResponse(review);
    }
}