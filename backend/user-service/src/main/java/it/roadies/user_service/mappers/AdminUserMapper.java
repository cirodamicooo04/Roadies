package it.roadies.user_service.mappers;

import it.roadies.user_service.data.dto.response.UserResponseDTO;
import it.roadies.user_service.data.entities.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AdminUserMapper {

    @Mapping(source = "gamification.points", target = "points")
    @Mapping(source = "gamification.badge", target = "badge")
    @Mapping(target = "enabled", ignore = true)
    UserResponseDTO toDto(User user);
}