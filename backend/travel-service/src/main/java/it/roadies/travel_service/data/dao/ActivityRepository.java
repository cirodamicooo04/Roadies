package it.roadies.travel_service.data.dao;

import it.roadies.travel_service.data.entity.Activity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ActivityRepository extends JpaRepository<Activity, UUID>, JpaSpecificationExecutor<Activity> {
    List<Activity> findAllByOwnerId(String ownerId);

    @Query("SELECT DISTINCT a.destination FROM Activity a WHERE a.destination IS NOT NULL AND a.travel IS NULL")
    List<String> findUniqueDestinations();

    Activity getActivitiesById(UUID id);
}
