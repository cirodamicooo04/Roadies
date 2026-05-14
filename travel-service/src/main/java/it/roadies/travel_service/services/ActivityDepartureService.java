package it.roadies.travel_service.services;

import java.math.BigDecimal;
import java.util.UUID;

public interface ActivityDepartureService {
    public void reserveSeats(UUID id, Integer peopleCount);
    public void releaseSeats(UUID id, Integer peopleCount);
    public boolean isValidActivity(UUID activityId);
    public BigDecimal getActivityPriceById(UUID activityId);
}
