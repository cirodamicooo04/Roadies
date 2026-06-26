package it.roadies.user_service.mappers;

import it.roadies.user_service.data.dto.response.PendingOrganizerRequestResponseDTO;
import it.roadies.user_service.data.dto.response.UserProfileResponseDTO;
import it.roadies.user_service.data.dto.request.UserSyncRequestDTO;
import it.roadies.user_service.data.dto.request.UserUpdateRequestDTO;
import it.roadies.user_service.data.entities.User;
import org.mapstruct.*;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {

    @Mapping(target = "keycloakId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "lastLogin", ignore = true)
    @Mapping(target = "gamification", ignore = true)
    @Mapping(target = "documents", ignore = true)
    @Mapping(target = "requesterFriendships", ignore = true)
    @Mapping(target = "receiverFriendships", ignore = true)
    User toEntity(UserSyncRequestDTO dto);


    @Mapping(source = "gamification.points", target = "points")
    @Mapping(source = "gamification.badge", target = "badge")
    UserProfileResponseDTO toDto(User user);

    @Mapping(source = "keycloakId", target = "keycloakId")
    @Mapping(source = "username", target = "username")
    @Mapping(source = "firstName", target = "firstName")
    @Mapping(source = "lastName", target = "lastName")
    @Mapping(source = "email", target = "email")
    @Mapping(source = "organizerRequestStatus", target = "organizerRequestStatus")
    @Mapping(source = "organizerRequestedAt", target = "organizerRequestedAt")
    PendingOrganizerRequestResponseDTO toPendingDto(User user);

    @Mapping(target = "keycloakId", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "lastLogin", ignore = true)
    @Mapping(target = "gamification", ignore = true)
    @Mapping(target = "documents", ignore = true)
    @Mapping(target = "requesterFriendships", ignore = true)
    @Mapping(target = "receiverFriendships", ignore = true)
    @Mapping(target = "organizerRequestStatus", ignore = true)
    @Mapping(target = "organizerRequestedAt", ignore = true)
    @Mapping(target = "organizerReviewedAt", ignore = true)
    @Mapping(target = "organizerRejectionReason", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromRequest(UserUpdateRequestDTO dto, @MappingTarget User entity);
}