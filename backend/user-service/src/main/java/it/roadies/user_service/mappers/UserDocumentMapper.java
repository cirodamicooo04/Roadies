package it.roadies.user_service.mappers;

import it.roadies.user_service.data.dto.request.UserDocumentRequestDTO;
import it.roadies.user_service.data.entities.UserDocument;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import it.roadies.user_service.data.dto.response.UserDocumentResponseDTO;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserDocumentMapper {

    @Mapping(source = "userId.keycloakId", target = "userId")
    UserDocumentResponseDTO toDto(UserDocument entity);

    List<UserDocumentResponseDTO> toDtoList(List<UserDocument> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updateBy", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "rejectionReason", ignore = true)
    @Mapping(target = "verifiedAt", ignore = true)
    @Mapping(target = "fileUrl", ignore = true)

    UserDocument toEntity(UserDocumentRequestDTO dto);
}