package it.roadies.travel_service.data.dao;

import it.roadies.travel_service.data.entity.ActivityDeparture;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ActivityDepartureRepository extends JpaRepository<ActivityDeparture, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM ActivityDeparture a WHERE a.id = :id")
    ActivityDeparture findByIdWithLock(@Param("id") UUID id);
}
