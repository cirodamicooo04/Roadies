package it.roadies.review_service.service.impl;

import it.roadies.review_service.conf.i8n.MessageLang;
import it.roadies.review_service.data.dao.ReviewRepository;
import it.roadies.review_service.data.dto.RatingSummary;
import it.roadies.review_service.data.dto.ReviewRequest;
import it.roadies.review_service.data.dto.ReviewResponse;
import it.roadies.review_service.data.dto.ReviewUpdateRequest;
import it.roadies.review_service.data.entity.Review;
import it.roadies.review_service.data.entity.ReviewType;
import it.roadies.review_service.data.event.ReviewActivityUpdateEvent;
import it.roadies.review_service.data.event.ReviewTravelUpdateEvent;
import it.roadies.review_service.data.mapper.ReviewMapper;
import it.roadies.review_service.exceptions.ReviewNotFoundException;
import it.roadies.review_service.service.ReviewService;
import it.roadies.review_service.service.client.TravelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewServiceImpl implements ReviewService {

    private final ReviewMapper reviewMapper;
    private final MessageLang messageLang;
    private final ReviewRepository repository;
    private final TravelService travelService;
    private final RabbitTemplate rabbitTemplate;

    @Transactional
    @Override
    public void createReview(ReviewRequest request, UUID travelId, String userId) {
        log.info("provo a creare una recensione - userId: {} travelId: {}", userId, travelId);
        Review review = reviewMapper.toEntity(request, userId);
        travelService.verifyTravelExists(travelId, request.getReviewType());
        review.setTravelId(travelId);
        // Check if the user has already reviewed this travel
        if (repository.existsByTravelIdAndUserId(travelId, userId)) {
            throw new AccessDeniedException("l'utente " + userId + " ha tentato di accedere ad una risorsa non autorizzato");
        }
        repository.save(review);


        RatingSummary summary = repository.findRatingSummaryByTravelId(travelId);

        if (request.getReviewType() == ReviewType.TRAVEL) {
            //mando evento di update per travel
            ReviewTravelUpdateEvent event = new ReviewTravelUpdateEvent(travelId, summary.getAverageRating(), summary.getTotalRatings());
            rabbitTemplate.convertAndSend("review.exchange", "review.travel.added", event);
        } else {
            //mando evento di update per activity
            ReviewActivityUpdateEvent event = new ReviewActivityUpdateEvent(travelId, summary.getAverageRating(), summary.getTotalRatings());
            rabbitTemplate.convertAndSend("review.exchange", "review.activity.added", event);
        }
    }

    @Override
    public List<ReviewResponse> getByTravel(UUID travelId) {
        log.info("provo a recuperare le recensioni di un viaggio - travelId: {}", travelId);
        return repository.findByTravelId(travelId)
                .stream()
                .map(reviewMapper::toReviewResponse)
                .toList();
    }

    @Override
    public double getAverageRating(UUID travelId) {
        log.info("provo a recuperare la valutazione media di un viaggio - travelId: {}", travelId);
        Double avg = repository.findAverageRatingByTravelId(travelId);
        if (avg==null) {
            return 0.0;
        }
        return avg;
    }

    @Transactional
    public void deleteReview(UUID reviewId, String userId) {
        log.info("provo a cancellare una recensione - reviewId: {} userId: {}", reviewId, userId);
        Review existingReview = repository.findById(reviewId).orElseThrow(() -> new ReviewNotFoundException(messageLang.getMessage("review.not.exists")));
        if (!existingReview.getUserId().equals(userId)) {
            throw new AccessDeniedException("l'utente " + userId + " ha tentato di accedere ad una risorsa non autorizzato");
        }
        repository.deleteById(reviewId);
    }

    // For administrative purposes
    @Override
    public List<Review> getAll() {
        log.info("provo a recuperare tutte le recensioni");
        return repository.findAll();
    }

    // Update the review by its id
    @Transactional
    @Override
    public Review updateReview(ReviewUpdateRequest reviewUpdateRequest, UUID reviewId, String userId) {
        log.info("provo a modificare una recensione - reviewId: {} userId: {}", reviewId, userId);
        Review existingReview = repository.findById(reviewId).orElseThrow(() -> new ReviewNotFoundException(messageLang.getMessage("review.not.found")));
        // Only the user who created the review can update it
        if (!existingReview.getUserId().equals(userId)) {
            throw new AccessDeniedException("l'utente " + userId + " ha tentato di accedere ad una risorsa non autorizzato");
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