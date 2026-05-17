package it.roadies.review_service.controller;

import it.roadies.review_service.dto.ReplyRequest;
import it.roadies.review_service.entity.ReviewReply;
import it.roadies.review_service.service.ReviewReplyService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/reviews/replies")
public class ReviewReplyController {

    private final ReviewReplyService replyService;

    public ReviewReplyController(ReviewReplyService replyService) {
        this.replyService = replyService;
    }

    // Create a reply for a specific review based on the review's id
    // POST /reviews/replies/{reviewId}
    @PostMapping("/{reviewId}")
    public ResponseEntity<ReviewReply> createReply(
            @PathVariable UUID reviewId,
            @Valid @RequestBody ReplyRequest request) {

        ReviewReply createdReply = replyService.createReply(reviewId, request);
        return new ResponseEntity<>(createdReply, HttpStatus.CREATED);
    }

    // Get the reply for a specific review based on the review's id
    // GET /reviews/replies/{reviewId}
    @GetMapping("/{reviewId}")
    public ResponseEntity<ReviewReply> getReplyByReviewId(@PathVariable UUID reviewId) {
        ReviewReply reply = replyService.getReplyByReviewId(reviewId);
        return ResponseEntity.ok(reply);
    }

    // Edit the content of a reply based on the reply's id
    // PUT /reviews/replies/id/{replyId}
    @PutMapping("/replyId/{replyId}")
    public ResponseEntity<ReviewReply> updateReply(
            @PathVariable UUID replyId,
            @Valid @RequestBody ReplyRequest request) {

        ReviewReply updatedReply = replyService.updateReply(replyId, request);
        return ResponseEntity.ok(updatedReply);
    }

    // delete the reply based on the reply's id
    // DELETE /reviews/replies/id/{replyId}
    @DeleteMapping("/replyId/{replyId}")
    public ResponseEntity<Void> deleteReply(@PathVariable UUID replyId) {
        replyService.deleteReply(replyId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}