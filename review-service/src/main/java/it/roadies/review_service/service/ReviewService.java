package it.roadies.review_service.service;

import it.roadies.review_service.ReviewRepository;
import it.roadies.review_service.dto.ReviewRequest;
import it.roadies.review_service.entity.Review;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ReviewService {

    private final ReviewRepository repository;

    public ReviewService(ReviewRepository repository) {
        this.repository = repository;
    }

    public Review createReview(ReviewRequest request, String userId) {

        Review review = new Review();
        review.setTravelId(request.getTravelId());
        review.setRating(request.getRating());
        review.setComment(request.getComment());
        review.setUserId(userId);

        return repository.save(review);
    }

    public List<Review> getByTravel(UUID travelId) {
        return repository.findByTravelId(travelId);
    }

    public double getAverageRating(UUID travelId) {
        Double avg = repository.findAverageRatingByTravelId(travelId);
        return avg != null ? avg : 0.0;
    }
}