package it.roadies.user_service.mappers;

import it.roadies.user_service.data.dto.request.UserDocumentRequestDTO;
import it.roadies.user_service.data.dto.response.UserDocumentResponseDTO;
import it.roadies.user_service.data.entities.User;
import it.roadies.user_service.data.entities.UserDocument;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-05-31T12:13:18+0200",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 26.0.1 (Oracle Corporation)"
)
@Component
public class UserDocumentMapperImpl implements UserDocumentMapper {

    @Override
    public UserDocumentResponseDTO toDto(UserDocument entity) {
        if ( entity == null ) {
            return null;
        }

        UserDocumentResponseDTO userDocumentResponseDTO = new UserDocumentResponseDTO();

        userDocumentResponseDTO.setUserId( entityUserIdKeycloakId( entity ) );
        userDocumentResponseDTO.setId( entity.getId() );
        userDocumentResponseDTO.setDocumentType( entity.getDocumentType() );
        userDocumentResponseDTO.setDocumentNumber( entity.getDocumentNumber() );
        userDocumentResponseDTO.setFileUrl( entity.getFileUrl() );
        userDocumentResponseDTO.setStatus( entity.getStatus() );
        userDocumentResponseDTO.setRejectionReason( entity.getRejectionReason() );
        userDocumentResponseDTO.setCreatedAt( entity.getCreatedAt() );

        return userDocumentResponseDTO;
    }

    @Override
    public List<UserDocumentResponseDTO> toDtoList(List<UserDocument> entities) {
        if ( entities == null ) {
            return null;
        }

        List<UserDocumentResponseDTO> list = new ArrayList<UserDocumentResponseDTO>( entities.size() );
        for ( UserDocument userDocument : entities ) {
            list.add( toDto( userDocument ) );
        }

        return list;
    }

    @Override
    public UserDocument toEntity(UserDocumentRequestDTO dto) {
        if ( dto == null ) {
            return null;
        }

        UserDocument userDocument = new UserDocument();

        userDocument.setDocumentType( dto.getDocumentType() );
        userDocument.setDocumentNumber( dto.getDocumentNumber() );

        return userDocument;
    }

    private String entityUserIdKeycloakId(UserDocument userDocument) {
        if ( userDocument == null ) {
            return null;
        }
        User userId = userDocument.getUserId();
        if ( userId == null ) {
            return null;
        }
        String keycloakId = userId.getKeycloakId();
        if ( keycloakId == null ) {
            return null;
        }
        return keycloakId;
    }
}
