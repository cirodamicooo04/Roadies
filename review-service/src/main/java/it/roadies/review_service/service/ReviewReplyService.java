package it.roadies.review_service.service;

import it.roadies.review_service.dto.ReplyRequest;
import it.roadies.review_service.entity.Review;
import it.roadies.review_service.entity.ReviewReply;
import it.roadies.review_service.ReviewRepository;
import it.roadies.review_service.ReviewReplyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ReviewReplyService {

    private final ReviewReplyRepository replyRepository;
    private final ReviewRepository reviewRepository; // To validate the existence of the original review before creating a reply

    public ReviewReplyService(ReviewReplyRepository replyRepository, ReviewRepository reviewRepository) {
        this.replyRepository = replyRepository;
        this.reviewRepository = reviewRepository;
    }

    // Create a new reply for a specific review
    @Transactional
    public ReviewReply createReply(UUID reviewId, ReplyRequest request) {

        // First validation: Ensure the original review exists before allowing a reply to be created
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Error: The review you are trying to reply to does not exist!"));

        // Second validation: Ensure that a reply does not already exist for this review (enforcing the one-to-one relationship)
        if (replyRepository.existsByReviewId(reviewId)) {
            throw new IllegalStateException("Error: A reply already exists for this review. Each review can only have one reply.");
        }

        // If both validations pass, proceed to create and save the new reply
        ReviewReply reply = new ReviewReply();
        reply.setContent(request.getContent());
        reply.setReview(review); // Establish the relationship by setting the review reference in the reply entity
        reply.setUserId("test-user-manager"); // Placeholder for user ID, should be replaced with actual user authentication logic

        return replyRepository.save(reply);
    }

    // Get the reply for a specific review by the review's ID
    public ReviewReply getReplyByReviewId(UUID reviewId) {
        return replyRepository.findByReviewId(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("No reply found for the specified review!"));
    }

    // Edit the content of an existing reply by its ID
    @Transactional
    public ReviewReply updateReply(UUID replyId, ReplyRequest request) {
        ReviewReply existingReply = replyRepository.findById(replyId)
                .orElseThrow(() -> new IllegalArgumentException("The reply you are trying to update does not exist!"));

        existingReply.setContent(request.getContent());
        return replyRepository.save(existingReply);
    }

    // Delete a reply by its ID
    @Transactional
    public void deleteReply(UUID replyId) {
        if (!replyRepository.existsById(replyId)) {
            throw new IllegalArgumentException("The reply you are trying to delete does not exist!");
        }
        replyRepository.deleteById(replyId);
    }
}