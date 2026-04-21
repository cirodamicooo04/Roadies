package it.roadies.booking_service.mapper;

import it.roadies.booking_service.data.dto.request.MemberDocumentRequestDTO;
import it.roadies.booking_service.data.dto.response.MemberDocumentResponseDTO;
import it.roadies.booking_service.data.entities.MemberDocument;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MemberDocumentMapper {
    MemberDocument toEntity(MemberDocumentRequestDTO requestDto);
    MemberDocumentResponseDTO toDto(MemberDocument entity);
}
