package it.roadies.travel_service.services;

import it.roadies.travel_service.data.dto.request.*;
import it.roadies.travel_service.data.dto.response.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface TravelService {
    TravelResponse createTravel(TravelCreateRequest travelCreateRequest, String ownerId);
    TravelResponse getTravelById(UUID id);
    void deleteTravelById(UUID id, String ownerId);
    TravelResponse updateTravel(TravelUpdateRequest travelUpdateRequest, String ownerId, UUID travelId);
    List<TravelSummaryResponse> searchTravels(String destination, BigDecimal minPrice, BigDecimal maxPrice, Integer minDurationDays, Integer maxDurationDays);
    OrganizerTravelsActivityResponse getOrganizerTravelsActivity(String ownerId);
    TravelDepartureResponse addDeparture(UUID travelId, TravelDepartureCreateRequest departureCreateRequest, String ownerId);
    void deleteDeparture(UUID travelId, UUID departureId, String ownerId);
    List<TravelDepartureResponse> getTravelDepartures(UUID travelId);
    TravelDepartureResponse updateDeparture(UUID travelId, UUID departureId, TravelDepartureUpdateRequest request, String ownerId);
    TravelDepartureResponse confirmDeparture(UUID travelId, UUID departureId, String ownerId);
}
