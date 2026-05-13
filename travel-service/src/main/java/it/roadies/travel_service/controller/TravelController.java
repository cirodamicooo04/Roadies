package it.roadies.travel_service.controller;

import it.roadies.travel_service.data.dto.request.*;
import it.roadies.travel_service.data.dto.response.*;
import it.roadies.travel_service.data.entity.enumerations.Continent;
import it.roadies.travel_service.services.ActivityService;
import it.roadies.travel_service.services.ImageService;
import it.roadies.travel_service.services.TravelDepartureService;
import it.roadies.travel_service.services.TravelService;
import it.roadies.travel_service.services.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/travels")
@RequiredArgsConstructor
public class TravelController {

    private final TravelService travelService;
    private final TravelDepartureService travelDepartureService;
    private final ActivityService activityService;
    private final ImageService imageService;

    //ORGANIZER AREA

    @PreAuthorize("hasRole('ORGANIZER')")
    @PostMapping
    public ResponseEntity<TravelResponse> createTravel(@RequestBody @Valid TravelCreateRequest travelCreateRequest, @AuthenticationPrincipal Jwt jwt) {
        TravelResponse response =  travelService.createTravel(travelCreateRequest, jwt.getClaim("sub"));
        return ResponseEntity.status(201).body(response);
    }

    @PreAuthorize("hasRole('ORGANIZER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTravel(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        travelService.deleteTravelById(id, jwt.getClaim("sub"));
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('ORGANIZER')")
    @PutMapping("/{id}")
    public ResponseEntity<TravelResponse> updateTravel(@PathVariable UUID id, @RequestBody @Valid TravelUpdateRequest request, @AuthenticationPrincipal Jwt jwt) {
        TravelResponse response = travelService.updateTravel(request, jwt.getClaim("sub"), id);
        return ResponseEntity.ok(response);
  }

    @PreAuthorize("hasRole('ORGANIZER')")
    @GetMapping("/my-travels")
    public ResponseEntity<OrganizerTravelsActivityResponse> getMyTravels(@AuthenticationPrincipal Jwt jwt){
        OrganizerTravelsActivityResponse response = travelService.getOrganizerTravelsActivity(jwt.getClaim("sub"));
        return ResponseEntity.ok(response);
    }

    //PUBLIC AREA

    @GetMapping("/public/{id}")
    public ResponseEntity<TravelResponse> getTravelById(@PathVariable UUID id){
        TravelResponse response = travelService.getTravelById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/public/search")
    public ResponseEntity<?> searchTravels(@RequestParam(required = false) String destination, @RequestParam(required = false) BigDecimal minPrice, @RequestParam(required = false) BigDecimal maxPrice, @RequestParam(required = false) Integer minDurationDays, @RequestParam(required = false) Integer maxDurationDays , @RequestParam(required = false, defaultValue = "TRAVEL") String type, @RequestParam(required = false)Continent continent, @RequestParam(required = false) String country, Pageable pageable){
        if(!type.equalsIgnoreCase("ACTIVITY")){
            Page<TravelSummaryResponse> travels = travelService.searchTravels(continent,country,destination, minPrice, maxPrice, minDurationDays, maxDurationDays, pageable);
            return ResponseEntity.ok(travels);
        }
        Page<ActivitySummaryResponse> activities = activityService.searchActivities(continent,country,destination, minPrice, maxPrice, pageable);
        return ResponseEntity.ok(activities);
    }

    //TRAVEL DEPARTURES AREA

    @PreAuthorize("hasRole('ORGANIZER')")
    @PostMapping("/{travelId}/departures")
    public ResponseEntity<TravelDepartureResponse> addDeparture(@PathVariable UUID travelId, @RequestBody @Valid TravelDepartureCreateRequest request, @AuthenticationPrincipal Jwt jwt){
        TravelDepartureResponse response = travelService.addDeparture(travelId, request, jwt.getClaim("sub"));
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ORGANIZER')")
    @DeleteMapping("/{travelId}/departures/{departureId}")
    public ResponseEntity<?> deleteDeparture(@PathVariable UUID travelId, @PathVariable UUID departureId, @AuthenticationPrincipal Jwt jwt){
        travelService.deleteDeparture(travelId, departureId, jwt.getClaim("sub"));
        return ResponseEntity.ok().build();
    }

    @GetMapping("/public/{travelId}/departures")
    public ResponseEntity<List<TravelDepartureResponse>> getDepartures(@PathVariable UUID travelId){
        List<TravelDepartureResponse> departures = travelService.getTravelDepartures(travelId);
        return ResponseEntity.ok(departures);
    }

    @PreAuthorize("hasRole('ORGANIZER')")
    @PutMapping("/{travelId}/departures/{departureId}")
    public ResponseEntity<TravelDepartureResponse> updateDeparture(@PathVariable UUID travelId, @PathVariable UUID departureId, @RequestBody @Valid TravelDepartureUpdateRequest request, @AuthenticationPrincipal Jwt jwt){
        TravelDepartureResponse response = travelService.updateDeparture(travelId, departureId, request, jwt.getClaim("sub"));
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ORGANIZER')")
    @PatchMapping("/{travelId}/departures/{departureId}/confirm")
    public ResponseEntity<TravelDepartureResponse> confirmDeparture(@PathVariable UUID travelId, @PathVariable UUID departureId, @AuthenticationPrincipal Jwt jwt){
        TravelDepartureResponse response = travelService.confirmDeparture(travelId, departureId, jwt.getClaim("sub"));
        return ResponseEntity.ok(response);
    }

    //TRAVEL ACTIVITIES AREA
    @PreAuthorize("hasRole('ORGANIZER')")
    @PostMapping("/{travelId}/activities")
    public ResponseEntity<TravelResponse> addActivity(@PathVariable UUID travelId, @RequestBody @Valid ActivityCreateRequest request, @AuthenticationPrincipal Jwt jwt){
        TravelResponse response = travelService.addActivity(travelId,request,jwt.getClaim("sub"));
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ORGANIZER')")
    @DeleteMapping("/{travelId}/activities/{activityId}")
    public ResponseEntity<?> deleteActivity(@PathVariable UUID travelId, @PathVariable UUID activityId, @AuthenticationPrincipal Jwt jwt){
        travelService.deleteTravelActivity(travelId,activityId,jwt.getClaim("sub"));
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('ORGANIZER')")
    @PutMapping("/{travelId}/activities/{activityId}")
    public ResponseEntity<TravelResponse> updateActivity(@PathVariable UUID travelId, @PathVariable UUID activityId, @RequestBody @Valid ActivityUpdateRequest request, @AuthenticationPrincipal Jwt jwt){
        TravelResponse response = travelService.updateTravelActivity(travelId,activityId,request,jwt.getClaim("sub"));
        return ResponseEntity.ok(response);
    }

    //RECOMMENDATIONS
    @PreAuthorize("hasRole('TRAVELER')")
    @GetMapping("/recommendations")
    public ResponseEntity<List<TravelSummaryResponse>> getRecommendations(@AuthenticationPrincipal Jwt jwt){
        List<TravelSummaryResponse> responses = travelService.getRecommendedTravels(jwt.getClaim("sub"));
        return ResponseEntity.ok(responses);
    }


    //BOOKING AREA

    @GetMapping("/{travelId}")
    public ResponseEntity<Void> isValidTravel(@PathVariable UUID travelId) {
        if (travelDepartureService.isValidTravel(travelId)) {
            return ResponseEntity.ok().build();
        }
        else return ResponseEntity.notFound().build();
    }

    @GetMapping("/{travelId}/price")
    public ResponseEntity<BigDecimal> getTravelPrice(@PathVariable UUID travelId) {
        BigDecimal response = travelDepartureService.getTravelPriceById(travelId);
        return ResponseEntity.ok(response);
    }

    //IMAGES AREA
    @PreAuthorize("hasRole('ORGANIZER')")
    @PostMapping(path = "/images",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImageResponse> uploadImage(@RequestParam("file") MultipartFile file, @AuthenticationPrincipal Jwt jwt){
        ImageResponse response = imageService.uploadImage(file, jwt.getClaim("sub"));
        return ResponseEntity.status(201).body(response);
    }


}
