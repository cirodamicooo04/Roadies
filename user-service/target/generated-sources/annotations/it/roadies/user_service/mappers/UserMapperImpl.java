package it.roadies.user_service.mappers;

import it.roadies.user_service.data.dto.request.UserSyncRequestDTO;
import it.roadies.user_service.data.dto.response.UserProfileResponseDTO;
import it.roadies.user_service.data.entities.Gamification;
import it.roadies.user_service.data.entities.User;
import it.roadies.user_service.data.entities.enumeration.Badge;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-05-31T12:13:19+0200",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 26.0.1 (Oracle Corporation)"
)
@Component
public class UserMapperImpl implements UserMapper {

    @Override
    public User toEntity(UserSyncRequestDTO dto) {
        if ( dto == null ) {
            return null;
        }

        User user = new User();

        user.setKeycloakId( dto.getKeycloakId() );
        user.setEmail( dto.getEmail() );
        user.setUsername( dto.getUsername() );
        user.setFirstName( dto.getFirstName() );
        user.setLastName( dto.getLastName() );
        user.setBirthDate( dto.getBirthDate() );

        return user;
    }

    @Override
    public UserProfileResponseDTO toDto(User user) {
        if ( user == null ) {
            return null;
        }

        UserProfileResponseDTO userProfileResponseDTO = new UserProfileResponseDTO();

        userProfileResponseDTO.setPoints( userGamificationPoints( user ) );
        userProfileResponseDTO.setBadge( userGamificationBadge( user ) );
        userProfileResponseDTO.setUsername( user.getUsername() );
        userProfileResponseDTO.setFirstName( user.getFirstName() );
        userProfileResponseDTO.setLastName( user.getLastName() );
        userProfileResponseDTO.setAvatarUrl( user.getAvatarUrl() );

        return userProfileResponseDTO;
    }

    @Override
    public void updateEntityFromRequest(UserSyncRequestDTO dto, User entity) {
        if ( dto == null ) {
            return;
        }

        if ( dto.getUsername() != null ) {
            entity.setUsername( dto.getUsername() );
        }
        if ( dto.getFirstName() != null ) {
            entity.setFirstName( dto.getFirstName() );
        }
        if ( dto.getLastName() != null ) {
            entity.setLastName( dto.getLastName() );
        }
        if ( dto.getBirthDate() != null ) {
            entity.setBirthDate( dto.getBirthDate() );
        }
    }

    private Long userGamificationPoints(User user) {
        if ( user == null ) {
            return null;
        }
        Gamification gamification = user.getGamification();
        if ( gamification == null ) {
            return null;
        }
        Long points = gamification.getPoints();
        if ( points == null ) {
            return null;
        }
        return points;
    }

    private Badge userGamificationBadge(User user) {
        if ( user == null ) {
            return null;
        }
        Gamification gamification = user.getGamification();
        if ( gamification == null ) {
            return null;
        }
        Badge badge = gamification.getBadge();
        if ( badge == null ) {
            return null;
        }
        return badge;
    }
}
