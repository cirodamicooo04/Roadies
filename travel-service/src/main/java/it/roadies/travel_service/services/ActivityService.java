package it.roadies.travel_service.services;

import it.roadies.travel_service.data.dto.request.ActivityCreateRequest;
import it.roadies.travel_service.data.dto.request.ActivityDepartureCreateRequest;
import it.roadies.travel_service.data.dto.request.ActivityDepartureUpdateRequest;
import it.roadies.travel_service.data.dto.request.ActivityUpdateRequest;
import it.roadies.travel_service.data.dto.response.ActivityDepartureResponse;
import it.roadies.travel_service.data.dto.response.ActivityResponse;
import it.roadies.travel_service.data.dto.response.ActivitySummaryResponse;
import it.roadies.travel_service.data.entity.Activity;
import it.roadies.travel_service.data.entity.enumerations.Continent;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface ActivityService {
    ActivityResponse createActivity(ActivityCreateRequest request, String ownerId);
    ActivityResponse getActivityById(UUID id);
    void deleteActivityById(UUID id, String ownerId);
    Page<ActivitySummaryResponse> searchActivities(Continent continent,String country,String destination, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);
    ActivityDepartureResponse addDeparture(UUID activityId, ActivityDepartureCreateRequest request, String ownerId);
    void deleteDeparture(UUID activityId, UUID departureId, String ownerId);
    ActivityDepartureResponse updateDeparture(UUID activityId, UUID departureId, ActivityDepartureUpdateRequest request, String ownerId);
    List<ActivityDepartureResponse> getDepartures(UUID activityId);
    ActivityDepartureResponse confirmDeparture(UUID activityId, UUID departureId, String ownerId);
    List<String> getUniqueDestinations(Continent continent, String country);
    ActivityResponse updateActivity(UUID id, ActivityUpdateRequest request, String ownerId);

    public boolean isValidActivityAndIsNotIntoATravel(UUID activityId);

}
