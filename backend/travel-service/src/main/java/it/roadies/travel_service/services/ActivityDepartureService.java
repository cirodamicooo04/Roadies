package it.roadies.travel_service.services;

import it.roadies.travel_service.data.dto.response.ActivityBatchResponse;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface ActivityDepartureService {
    public void reserveSeats(UUID id, Integer peopleCount);
    public void releaseSeats(UUID id, Integer peopleCount);
    public boolean isValidActivity(UUID activityId);
    public BigDecimal getActivityPriceById(UUID activityId);
    public List<ActivityBatchResponse> getActivitiesBatch(List<UUID> activityIds);
}
