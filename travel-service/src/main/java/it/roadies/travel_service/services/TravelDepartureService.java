package it.roadies.travel_service.services;

import java.util.UUID;

public interface TravelDepartureService {
    public void reserveSeats(UUID travelDepartureId, Integer spots);
    public void releaseSeats(UUID travelDepartureId, Integer spots);
}
