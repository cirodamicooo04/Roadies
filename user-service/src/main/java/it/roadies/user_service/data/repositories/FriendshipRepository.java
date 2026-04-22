package it.roadies.user_service.data.repositories;

import it.roadies.user_service.data.entities.Friendship;
import it.roadies.user_service.data.entities.User;
import it.roadies.user_service.data.entities.enumeration.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FriendshipRepository extends JpaRepository<Friendship, UUID> {

    @Query("SELECT f FROM Friendship f WHERE " +
            "(f.requester_id.keycloak_id = :id1 AND f.receiver_id.keycloak_id = :id2) OR " +
            "(f.requester_id.keycloak_id = :id2 AND f.receiver_id.keycloak_id = :id1)")
    Optional<Friendship> findExistingFriendship(String id1, String id2);

    @Query("SELECT CASE WHEN f.requester_id.keycloak_id = :userId THEN f.receiver_id ELSE f.requester_id END " +
            "FROM Friendship f WHERE (f.requester_id.keycloak_id = :userId OR f.receiver_id.keycloak_id = :userId) " +
            "AND f.status = it.roadies.user_service.data.entities.enumeration.Status.ACCEPTED")
    List<User> findAcceptedFriendsByUser(String userId);

    List<Friendship> findByReceiver_idAndStatus(User receiver, Status status);
}