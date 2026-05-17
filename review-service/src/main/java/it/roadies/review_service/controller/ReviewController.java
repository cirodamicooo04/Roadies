package it.roadies.review_service.controller;

import it.roadies.review_service.dto.ReviewRequest;
import it.roadies.review_service.entity.Review;
import it.roadies.review_service.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    // Creat a review by user (id)
    @PostMapping
    public ResponseEntity<Review> create(@Valid @RequestBody ReviewRequest request) {
        String userId = "test-user"; // to make sure it is the correct user (should be done later)
        Review createdReview = service.createReview(request, userId);
        return new ResponseEntity<>(createdReview, HttpStatus.CREATED);
    }

    // Get all the reviews of a travel by its id
    @GetMapping("/travel/{travelId}")
    public ResponseEntity<List<Review>> getByTravel(@PathVariable UUID travelId) {
        return ResponseEntity.ok(service.getByTravel(travelId));
    }

    // Get the average rating of a travel
    @GetMapping("/travel/{travelId}/average")
    public ResponseEntity<Double> getAverageRating(@PathVariable UUID travelId) {
        return ResponseEntity.ok(service.getAverageRating(travelId));
    }

    // Edit the review by its id
    @PutMapping("/{reviewId}")
    public ResponseEntity<Review> updateReview(
            @PathVariable UUID reviewId,
            @Valid @RequestBody ReviewRequest request) {
        String userId = "test-user"; // to make sure it is the correct user (should be done later)
        Review updatedReview = service.updateReview(reviewId, request, userId);
        return ResponseEntity.ok(updatedReview);
    }

    // Delete the review by its id
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview(@PathVariable ("id")UUID reviewlId) {
        service.deleteReview(reviewlId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    // for testing purposes only
    @GetMapping("/all")
    public ResponseEntity<List<Review>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }
}