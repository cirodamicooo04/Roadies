package it.roadies.travel_service.data.dao;

import it.roadies.travel_service.data.entity.Travel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TravelRepository extends JpaRepository<Travel, UUID>, JpaSpecificationExecutor<Travel> {
    List<Travel> findAllByOwnerId(String ownerId);

    @Query("SELECT DISTINCT t.destination FROM Travel t WHERE t.destination IS NOT NULL")
    List<String> findUniqueDestinations();
}
