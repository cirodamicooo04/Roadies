package it.roadies.review_service.service;

import it.roadies.review_service.data.dto.ReplyRequest;
import it.roadies.review_service.data.dto.ReplyResponse;
import it.roadies.review_service.data.entity.ReviewReply;

import java.util.UUID;

public interface ReviewReplyService {

    void createReply(ReplyRequest request, UUID reviewId, String userId);

    ReplyResponse getReplyByReviewId(UUID reviewId);

    ReviewReply updateReply(UUID replyId, ReplyRequest request, String userId);

    void deleteReply(UUID replyId, String userId);

}
