package it.roadies.travel_service.services;

import java.util.UUID;

public interface ActivityDepartureService {
    public void reserveSeats(UUID id, Integer peopleCount);
    public void releaseSeats(UUID id, Integer peopleCount);
}
