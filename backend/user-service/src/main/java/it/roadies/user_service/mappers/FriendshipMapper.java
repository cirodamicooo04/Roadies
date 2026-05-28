package it.roadies.user_service.mappers;

import it.roadies.user_service.data.dto.response.FriendshipResponseDTO;
import it.roadies.user_service.data.entities.Friendship;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = {UserMapper.class})
public interface FriendshipMapper {

    @Mapping(target = "friendProfile", ignore = true)
    FriendshipResponseDTO toDto(Friendship entity);

    List<FriendshipResponseDTO> toDtoList(List<Friendship> entities);
}
