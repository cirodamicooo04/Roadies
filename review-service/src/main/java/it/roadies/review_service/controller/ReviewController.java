package it.roadies.review_service.controller;

import it.roadies.review_service.data.dto.ReviewRequest;
import it.roadies.review_service.data.dto.ReviewResponse;
import it.roadies.review_service.data.dto.ReviewUpdateRequest;
import it.roadies.review_service.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService service;

    // Create a review by travel/activity (id)
    @PostMapping
    public ResponseEntity<Void> create(@Valid @RequestBody ReviewRequest request, @AuthenticationPrincipal Jwt jwt) {
        service.createReview(request, jwt.getSubject());

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // Get all the reviews of a travel or activity by its id
    @GetMapping("/travel/{travelId}")
    public ResponseEntity<List<ReviewResponse>> getByTravel(@PathVariable UUID travelId) {
        return ResponseEntity.ok(service.getByTravel(travelId));
    }

    // Get the average rating of a travel
    @GetMapping("/travel/{travelId}/average")
    public ResponseEntity<Double> getAverageRating(@PathVariable UUID travelId) {
        return ResponseEntity.ok(service.getAverageRating(travelId));
    }

    // Edit the review by its id
    @PutMapping("/{reviewId}")
    public ResponseEntity<ReviewUpdateRequest> updateReview(
            @PathVariable UUID reviewId,
            @Valid @RequestBody ReviewUpdateRequest request) {
        service.updateReview(request, reviewId);
        return ResponseEntity.ok().build();
    }

    // Delete the review by its id
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview(@PathVariable("id") UUID reviewlId) {
        service.deleteReview(reviewlId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    // for testing purposes only
    @GetMapping("/all")
    public ResponseEntity<List<ReviewResponse>> getAll() {
        return ResponseEntity.ok(service.getAll().stream().map(review -> service.createReviewResponse(review.getId())).toList());
    }
}