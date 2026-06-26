package it.roadies.travel_service.services;

import java.math.BigDecimal;
import java.util.UUID;

public interface TravelDepartureService {
    public void reserveSeats(UUID travelDepartureId, Integer spots);
    public void releaseSeats(UUID travelDepartureId, Integer spots);
    public boolean isValidTravel(UUID travelDepartureId);
    public BigDecimal getTravelPriceById(UUID travelId);

}
