package it.roadies.travel_service.dao;

import it.roadies.travel_service.entity.TravelDeparture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TravelDepartureRepository extends JpaRepository<TravelDeparture, UUID> {
}
