package it.roadies.user_service.mappers;

import it.roadies.user_service.data.dto.response.FriendshipResponseDTO;
import it.roadies.user_service.data.entities.Friendship;
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
public class FriendshipMapperImpl implements FriendshipMapper {

    @Override
    public FriendshipResponseDTO toDto(Friendship entity) {
        if ( entity == null ) {
            return null;
        }

        FriendshipResponseDTO friendshipResponseDTO = new FriendshipResponseDTO();

        friendshipResponseDTO.setId( entity.getId() );
        if ( entity.getStatus() != null ) {
            friendshipResponseDTO.setStatus( entity.getStatus().name() );
        }
        friendshipResponseDTO.setCreatedAt( entity.getCreatedAt() );

        return friendshipResponseDTO;
    }

    @Override
    public List<FriendshipResponseDTO> toDtoList(List<Friendship> entities) {
        if ( entities == null ) {
            return null;
        }

        List<FriendshipResponseDTO> list = new ArrayList<FriendshipResponseDTO>( entities.size() );
        for ( Friendship friendship : entities ) {
            list.add( toDto( friendship ) );
        }

        return list;
    }
}
