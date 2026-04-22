package it.roadies.booking_service.mapper;

import it.roadies.booking_service.data.dto.request.MemberDocumentRequest;
import it.roadies.booking_service.data.dto.response.MemberDocumentResponse;
import it.roadies.booking_service.data.entities.MemberDocument;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MemberDocumentMapper {
    MemberDocument toEntity(MemberDocumentRequest requestDto);
    MemberDocumentResponse toDto(MemberDocument entity);
}
