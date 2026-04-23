package it.roadies.user_service.data.repositories;

import it.roadies.user_service.data.entities.Friendship;
import it.roadies.user_service.data.entities.User;
import it.roadies.user_service.data.entities.enumeration.Status;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    //Si differenzia da quella sopra perchè qui andiamo a prendere tutti i dati dell'amicizia compreso
    // l'id per poter abilitare la funzione di rimozione dell'amicizia e anche la data di creazione
    // dell'amicizia se vogliamo visualizzare ad esempio amici dal gg/mm/aaaa
    @Query("SELECT f FROM Friendship f WHERE " +
            "(f.requester_id.keycloak_id = :userId OR f.receiver_id.keycloak_id = :userId) " +
            "AND f.status = it.roadies.user_service.data.entities.enumeration.Status.ACCEPTED")
    List<Friendship> findAllAcceptedFriendshipsByUser(String userId);


    //Tenere a mente che quando abbiamo delle query su dei parametri con _ come receiver_id in questo modo crasha
    //List<Friendship> findByReceiver_idAndStatus(User receiver, Status status);
    //Meglio scrivere la query a mano
    @Query("SELECT f FROM Friendship f WHERE f.receiver_id = :receiver AND f.status = :status")
    List<Friendship> findByReceiver_idAndStatus(
            @Param("receiver") User receiver,
            @Param("status") Status status
    );

}