package it.roadies.review_service.data.mapper;

import it.roadies.review_service.data.dto.ReplyRequest;
import it.roadies.review_service.data.dto.ReplyResponse;
import it.roadies.review_service.data.entity.Review;
import it.roadies.review_service.data.entity.ReviewReply;
import java.util.UUID;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-05-31T11:57:05+0200",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 23.0.2 (Oracle Corporation)"
)
@Component
public class ReplyMapperImpl implements ReplyMapper {

    @Override
    public ReviewReply toEntity(ReplyRequest replyRequest, String userId) {
        if ( replyRequest == null && userId == null ) {
            return null;
        }

        ReviewReply reviewReply = new ReviewReply();

        if ( replyRequest != null ) {
            reviewReply.setContent( replyRequest.getContent() );
        }
        reviewReply.setUserId( userId );

        return reviewReply;
    }

    @Override
    public ReplyResponse toReplyResponse(ReviewReply reply) {
        if ( reply == null ) {
            return null;
        }

        ReplyResponse replyResponse = new ReplyResponse();

        replyResponse.setReviewId( replyReviewId( reply ) );
        replyResponse.setId( reply.getId() );
        replyResponse.setContent( reply.getContent() );
        replyResponse.setUserId( reply.getUserId() );
        replyResponse.setCreatedAt( reply.getCreatedAt() );

        return replyResponse;
    }

    private UUID replyReviewId(ReviewReply reviewReply) {
        if ( reviewReply == null ) {
            return null;
        }
        Review review = reviewReply.getReview();
        if ( review == null ) {
            return null;
        }
        UUID id = review.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }
}
