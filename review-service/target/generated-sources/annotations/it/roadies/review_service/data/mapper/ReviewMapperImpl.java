package it.roadies.review_service.data.mapper;

import it.roadies.review_service.data.dto.ReviewRequest;
import it.roadies.review_service.data.dto.ReviewResponse;
import it.roadies.review_service.data.entity.Review;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-05-31T11:57:05+0200",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 23.0.2 (Oracle Corporation)"
)
@Component
public class ReviewMapperImpl implements ReviewMapper {

    @Override
    public Review toEntity(ReviewRequest reviewRequest, String userId) {
        if ( reviewRequest == null && userId == null ) {
            return null;
        }

        Review review = new Review();

        if ( reviewRequest != null ) {
            review.setRating( reviewRequest.getRating() );
            review.setContent( reviewRequest.getContent() );
            review.setReviewType( reviewRequest.getReviewType() );
        }
        review.setUserId( userId );

        return review;
    }

    @Override
    public ReviewResponse toReviewResponse(Review review) {
        if ( review == null ) {
            return null;
        }

        ReviewResponse reviewResponse = new ReviewResponse();

        reviewResponse.setId( review.getId() );
        reviewResponse.setTravelId( review.getTravelId() );
        reviewResponse.setReviewType( review.getReviewType() );
        reviewResponse.setUserId( review.getUserId() );
        reviewResponse.setRating( review.getRating() );
        reviewResponse.setContent( review.getContent() );
        reviewResponse.setCreatedAt( review.getCreatedAt() );

        return reviewResponse;
    }
}
