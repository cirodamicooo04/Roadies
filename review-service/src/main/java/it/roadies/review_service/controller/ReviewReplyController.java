package it.roadies.review_service.controller;

import it.roadies.review_service.data.dto.ReplyRequest;
import it.roadies.review_service.data.dto.ReplyResponse;
import it.roadies.review_service.data.entity.ReviewReply;
import it.roadies.review_service.service.impl.ReviewReplyServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reviews/replies")
@RequiredArgsConstructor
public class ReviewReplyController {

    private final ReviewReplyServiceImpl replyService;

    @PostMapping("/{reviewId}")
    public ResponseEntity<Void> createReply(
            @PathVariable UUID reviewId,
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody ReplyRequest request) {

        replyService.createReply(request, reviewId, jwt.getSubject());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // Get the reply for a specific review based on the review's id
    // GET /reviews/replies/{reviewId}
    @GetMapping("/{reviewId}")
    public ResponseEntity<ReplyResponse> getReplyByReviewId(@PathVariable UUID reviewId) {
        ReplyResponse reply = replyService.getReplyByReviewId(reviewId);
        return ResponseEntity.ok(reply);
    }

    // Edit the content of a reply based on the reply's id
    // PUT /reviews/replies/id/{replyId}
    @PutMapping("/replyId/{replyId}")
    public ResponseEntity<ReviewReply> updateReply(
            @PathVariable UUID replyId,
            @Valid @RequestBody ReplyRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        ReviewReply updatedReply = replyService.updateReply(replyId, request, jwt.getSubject());
        return ResponseEntity.ok(updatedReply);
    }

    // delete the reply based on the reply's id
    // DELETE /reviews/replies/id/{replyId}
    @DeleteMapping("/replyId/{replyId}")
    public ResponseEntity<Void> deleteReply(@PathVariable UUID replyId, @AuthenticationPrincipal Jwt jwt) {
        replyService.deleteReply(replyId, jwt.getSubject());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}