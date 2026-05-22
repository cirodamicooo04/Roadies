package it.roadies.review_service.service.impl;

import it.roadies.review_service.conf.i8n.MessageLang;
import it.roadies.review_service.data.dto.ReplyRequest;
import it.roadies.review_service.data.dto.ReplyResponse;
import it.roadies.review_service.data.entity.Review;
import it.roadies.review_service.data.entity.ReviewReply;
import it.roadies.review_service.data.dao.ReviewRepository;
import it.roadies.review_service.data.dao.ReviewReplyRepository;
import it.roadies.review_service.data.mapper.ReplyMapper;
import it.roadies.review_service.exceptions.ReplyNotFoundException;
import it.roadies.review_service.exceptions.ReviewBusinessException;
import it.roadies.review_service.exceptions.ReviewNotFoundException;
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
    private final ReviewRepository reviewRepository;
    private final MessageLang messageLang;

    // Create a new reply for a specific review
    @Transactional
    @Override
    public void createReply(ReplyRequest request, UUID reviewId, String userId) {

        // First validation: Ensure the original review exists before allowing a reply to be created
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException(messageLang.getMessage("error.resource.not.found")));

        // Second validation: Ensure that a reply does not already exist for this review (enforcing the one-to-one relationship)
        if (replyRepository.existsByReviewId(reviewId)) {
            throw new ReviewBusinessException(messageLang.getMessage("review.reply.already.exists"));
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
                .orElseThrow(() -> new ReplyNotFoundException(messageLang.getMessage("review.not.found")));
    }

    // Edit the content of an existing reply by its ID
    @Transactional
    @Override
    public ReviewReply updateReply(UUID replyId, ReplyRequest request) {
        ReviewReply existingReply = replyRepository.findById(replyId)
                .orElseThrow(() -> new ReplyNotFoundException(messageLang.getMessage("review.reply.not.found",replyId)));

        existingReply.setContent(request.getContent());
        return replyRepository.save(existingReply);
    }

    // Delete a reply by its ID
    @Transactional
    @Override
    public void deleteReply(UUID replyId) {
        if (!replyRepository.existsById(replyId)) {
            throw new ReplyNotFoundException(messageLang.getMessage("review.reply.not.found", replyId));
        }
        replyRepository.deleteById(replyId);
    }
}