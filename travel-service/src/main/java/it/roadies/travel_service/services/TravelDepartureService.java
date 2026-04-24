package it.roadies.travel_service.services;

import it.roadies.travel_service.data.entity.TravelDeparture;
import jakarta.transaction.Transactional;

import java.util.UUID;

public interface TravelDepartureService {
    void reserveSpots(UUID travelDepartureId, int spots);
    void releaseSpots(UUID travelDepartureId, int spots);
}
