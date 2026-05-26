package it.roadies.review_service.data.mapper;

import it.roadies.review_service.data.dto.ReviewRequest;
import it.roadies.review_service.data.dto.ReviewResponse;
import it.roadies.review_service.data.entity.Review;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ReviewMapper {
    @Mapping(source = "userId", target = "userId")
    Review toEntity(ReviewRequest reviewRequest, String userId);

    ReviewResponse toReviewResponse(Review review);


}
