package it.roadies.review_service.controller;

import it.roadies.review_service.dto.ReviewRequest;
import it.roadies.review_service.entity.Review;
import it.roadies.review_service.service.ReviewService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/reviews")
public class ReviewController {

    private final ReviewService service;

    public ReviewController(ReviewService service) {
        this.service = service;
    }


    @PostMapping
    public Review create(@RequestBody ReviewRequest request) {
        String userId = "test-user";

        return service.createReview(request, userId);
    }

    // GET /reviews/travel/{id}
    @GetMapping("/travel/{travelId}")
    public List<Review> getByTravel(@PathVariable UUID travelId) {
        return service.getByTravel(travelId);
    }

    // GET /reviews/travel/{id}/average
    @GetMapping("/travel/{travelId}/average")
    public double getAverageRating(@PathVariable UUID travelId) {
        return service.getAverageRating(travelId);
    }
}