package it.roadies.travel_service.data.dao;

import it.roadies.travel_service.data.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TagRepository extends JpaRepository<Tag, UUID> {
    Optional<Tag> findByNameIgnoreCase(String name);
    @Modifying
    @Query(value = "INSERT INTO TRAVEL_TAG (travel_id, tag_id, score) SELECT id, :tagId, :defaultScore FROM travel", nativeQuery = true)
    void addDefaultTagToAllTravels(@Param("tagId") UUID tagId, @Param("defaultScore") Integer defaultScore);
}
