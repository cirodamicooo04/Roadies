package it.roadies.user_service.mappers;

import it.roadies.user_service.data.dto.response.UserProfileResponseDTO;
import it.roadies.user_service.data.dto.request.UserSyncRequestDTO;
import it.roadies.user_service.data.entities.User;
import org.mapstruct.*;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {

    User toEntity(UserSyncRequestDTO dto);


    @Mapping(source = "gamification.points", target = "points")
    @Mapping(source = "gamification.badge", target = "badge")
    UserProfileResponseDTO toDto(User user);

    @Mapping(target = "keycloak_id", ignore = true)
    @Mapping(target = "email", ignore = true) // Solitamente l'email non si cambia da qui
    @Mapping(target = "created_at", ignore = true)
    @Mapping(target = "gamification", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromRequest(UserSyncRequestDTO dto, @MappingTarget User entity);
}