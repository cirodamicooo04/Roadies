package it.roadies.review_service.service.impl;

import it.roadies.review_service.data.dto.ReplyRequest;
import it.roadies.review_service.data.dto.ReplyResponse;
import it.roadies.review_service.data.entity.Review;
import it.roadies.review_service.data.entity.ReviewReply;
import it.roadies.review_service.data.dao.ReviewRepository;
import it.roadies.review_service.data.dao.ReviewReplyRepository;
import it.roadies.review_service.data.mapper.ReplyMapper;
import it.roadies.review_service.service.ReviewReplyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReviewReplyServiceImpl implements ReviewReplyService {

    private final ReplyMapper replyMapper;
    private final ReviewReplyRepository replyRepository;
    private final ReviewRepository reviewRepository; // To validate the existence of the original review before creating a reply

    // Create a new reply for a specific review
    @Transactional
    @Override
    public void createReply(ReplyRequest request, UUID reviewId, String userId) {

        // First validation: Ensure the original review exists before allowing a reply to be created
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Error: The review you are trying to reply to does not exist!"));

        // Second validation: Ensure that a reply does not already exist for this review (enforcing the one-to-one relationship)
        if (replyRepository.existsByReviewId(reviewId)) {
            throw new IllegalStateException("Error: A reply already exists for this review. Each review can only have one reply.");
        }

        // If both validations pass, proceed to create and save the new reply
        ReviewReply reply = replyMapper.toEntity(request, userId);
        reply.setReview(review);
        replyRepository.save(reply);
    }

    // Get the reply for a specific review by the review's ID

    @Override
    public ReplyResponse getReplyByReviewId(UUID reviewId) {
        return replyRepository.findByReviewId(reviewId)
                .map(replyMapper::toReplyResponse)
                .orElseThrow(() -> new IllegalArgumentException("No reply found for review id: " + reviewId));
    }

    // Edit the content of an existing reply by its ID
    @Transactional
    @Override
    public ReviewReply updateReply(UUID replyId, ReplyRequest request) {
        ReviewReply existingReply = replyRepository.findById(replyId)
                .orElseThrow(() -> new IllegalArgumentException("The reply you are trying to update does not exist!"));

        existingReply.setContent(request.getContent());
        return replyRepository.save(existingReply);
    }

    // Delete a reply by its ID
    @Transactional
    @Override
    public void deleteReply(UUID replyId) {
        if (!replyRepository.existsById(replyId)) {
            throw new IllegalArgumentException("The reply you are trying to delete does not exist!");
        }
        replyRepository.deleteById(replyId);
    }

    @Override
    public ReplyResponse createReplyResponse(UUID replyTd) {
        ReviewReply reply = replyRepository.findById(replyTd)
                .orElseThrow(() -> new IllegalArgumentException("No reply found for the specified ID!"));
        return replyMapper.toReplyResponse(reply);
    }
}