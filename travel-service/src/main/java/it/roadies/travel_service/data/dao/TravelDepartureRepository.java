package it.roadies.travel_service.data.dao;

import it.roadies.travel_service.data.entity.Travel;
import it.roadies.travel_service.data.entity.TravelDeparture;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TravelDepartureRepository extends JpaRepository<TravelDeparture, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM TravelDeparture t WHERE t.id = :id")
    TravelDeparture findByIdWithLock(@Param("id") UUID id);

    boolean findByTravel(Travel travel);

    boolean existsByTravel(Travel travel);

    List<TravelDeparture> findAllByTravel(Travel travel);

    @Query("SELECT t.price FROM TravelDeparture t WHERE t.id = :id")
    Optional<BigDecimal> findPriceById(@Param("id") UUID id);
}
