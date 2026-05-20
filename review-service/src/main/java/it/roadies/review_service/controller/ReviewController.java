package it.roadies.review_service.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.roadies.review_service.data.dto.ReviewRequest;
import it.roadies.review_service.data.dto.ReviewResponse;
import it.roadies.review_service.data.dto.ReviewUpdateRequest;
import it.roadies.review_service.service.impl.ReviewServiceImpl;
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
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
@Tag(name = "Review Controller", description = "Endpoints per la gestione delle recensioni di viaggi e attività")
public class ReviewController {

    private final ReviewServiceImpl service;

    // Create a review by travel/activity (id)
    @Operation(
            summary = "Crea una nuova recensione",
            description = "Permette a un utente autenticato di lasciare una recensione (voto e commento) per un determinato viaggio o attività."
    )
    @PostMapping("/{travelId}")
    public ResponseEntity<Void> create(
            @PathVariable("travelId") UUID travelId,
            @Valid @RequestBody ReviewRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        service.createReview(request, travelId, jwt.getSubject());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }


    // Get all the reviews of a travel or activity by its id
    @GetMapping("/{travelId}")
    public ResponseEntity<List<ReviewResponse>> getByTravel(@PathVariable UUID travelId) {
        return ResponseEntity.ok(service.getByTravel(travelId));
    }

    // Get the average rating of a travel or activity by its id
    @GetMapping("/{travelId}/average")
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