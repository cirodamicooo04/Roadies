package it.roadies.review_service.data.mapper;

import it.roadies.review_service.data.dto.ReplyRequest;
import it.roadies.review_service.data.dto.ReplyResponse;
import it.roadies.review_service.data.entity.ReviewReply;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ReplyMapper {

    @Mapping(source = "userId", target = "userId")
    ReviewReply toEntity(ReplyRequest replyRequest, String userId);

    ReplyResponse toReplyResponse(ReviewReply reply);
}
