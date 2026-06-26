package it.roadies.user_service.data.repositories;

import it.roadies.user_service.data.entities.UserDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface UserDocumentRepository extends JpaRepository<UserDocument, UUID> {

    @Query("SELECT d FROM UserDocument d WHERE d.userId.keycloakId = :keycloakId")
    List<UserDocument> findByUserId(@Param("keycloakId") String keycloakId);

}
