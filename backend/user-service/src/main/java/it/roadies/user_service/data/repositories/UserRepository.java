package it.roadies.user_service.data.repositories;

import it.roadies.user_service.data.entities.User;
import it.roadies.user_service.data.entities.enumeration.OrganizerRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findByUsername(String username);

    List<User> findByOrganizerRequestStatus(OrganizerRequestStatus status);

    List<User> findAllByEnabledTrue();

    List<User> findAllByEnabledFalse();

    List<User> findAllByEnabledTrueAndOrganizerRequestStatus(OrganizerRequestStatus organizerRequestStatus);
}