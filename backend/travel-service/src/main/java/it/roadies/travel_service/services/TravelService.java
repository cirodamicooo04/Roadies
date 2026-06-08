package it.roadies.travel_service.services;

import it.roadies.travel_service.data.dto.event.ReviewTravelUpdateEvent;
import it.roadies.travel_service.data.dto.request.*;
import it.roadies.travel_service.data.dto.response.*;
import it.roadies.travel_service.data.entity.enumerations.Continent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface TravelService {
    TravelResponse createTravel(TravelCreateRequest travelCreateRequest, String ownerId);
    TravelResponse getTravelById(UUID id);
    void deleteTravelById(UUID id, String ownerId);
    TravelResponse updateTravel(TravelUpdateRequest travelUpdateRequest, String ownerId, UUID travelId);
    Page<TravelSummaryResponse> searchTravels(Continent continent,String country, String destination, BigDecimal minPrice, BigDecimal maxPrice, Integer minDurationDays, Integer maxDurationDays, Pageable pageable);
    OrganizerTravelsActivityResponse getOrganizerTravelsActivity(String ownerId);
    TravelDepartureResponse addDeparture(UUID travelId, TravelDepartureCreateRequest departureCreateRequest, String ownerId);
    void deleteDeparture(UUID travelId, UUID departureId, String ownerId);
    List<TravelDepartureResponse> getTravelDepartures(UUID travelId);
    TravelDepartureResponse updateDeparture(UUID travelId, UUID departureId, TravelDepartureUpdateRequest request, String ownerId);
    TravelDepartureResponse confirmDeparture(UUID travelId, UUID departureId, String ownerId);
    List<String> getUniqueDestinations(Continent continent, String country);
    TravelResponse addActivity(UUID travelId, ActivityCreateRequest request, String ownerId);
    void deleteTravelActivity(UUID travelId, UUID activityId, String ownerId);
    TravelResponse updateTravelActivity(UUID travelId, UUID activityId, ActivityUpdateRequest request, String ownerId);
    List<TravelSummaryResponse> getRecommendedTravels();
    void updateTravelReviews(ReviewTravelUpdateEvent event);
    boolean isValidTravel(UUID travelId);
}
