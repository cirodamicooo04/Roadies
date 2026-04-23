package it.roadies.travel_service.controller;

import it.roadies.travel_service.data.dto.request.TravelCreateRequest;
import it.roadies.travel_service.data.dto.request.TravelDepartureCreateRequest;
import it.roadies.travel_service.data.dto.request.TravelDepartureUpdateRequest;
import it.roadies.travel_service.data.dto.request.TravelUpdateRequest;
import it.roadies.travel_service.data.dto.response.OrganizerTravelsActivityResponse;
import it.roadies.travel_service.data.dto.response.TravelDepartureResponse;
import it.roadies.travel_service.data.dto.response.TravelResponse;
import it.roadies.travel_service.data.dto.response.TravelSummaryResponse;
import it.roadies.travel_service.services.TravelDepartureService;
import it.roadies.travel_service.services.TravelService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/travels")
@RequiredArgsConstructor
public class TravelController {

    private final TravelService travelService;
    private final TravelDepartureService travelDepartureService;

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

    //DA VEDERE PROBLEMA MAPPER AGGIORNAMENTO
    //@PreAuthorize("hasRole('ORGANIZER')")
//    @PutMapping("/{id}")
//    public ResponseEntity<TravelResponse> updateTravel(@PathVariable UUID id, @RequestBody @Valid TravelUpdateRequest request, @AuthenticationPrincipal Jwt jwt) {
//        TravelResponse response = travelService.updateTravel(request, jwt.getClaim("sub"), id);
//        return ResponseEntity.ok(response);
//  }

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
    public ResponseEntity<List<TravelSummaryResponse>> searchTravels(@RequestParam(required = false) String destination, @RequestParam(required = false) BigDecimal minPrice, @RequestParam(required = false) BigDecimal maxPrice, @RequestParam(required = false) Integer minDurationDays, @RequestParam(required = false) Integer maxDurationDays ){
        List<TravelSummaryResponse> travels = travelService.searchTravels(destination, minPrice, maxPrice, minDurationDays, maxDurationDays);
        return ResponseEntity.ok(travels);
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
    @PutMapping("/{travelId}/departures/{departureId}/confirm")
    public ResponseEntity<TravelDepartureResponse> confirmDeparture(@PathVariable UUID travelId, @PathVariable UUID departureId, @AuthenticationPrincipal Jwt jwt){
        TravelDepartureResponse response = travelService.confirmDeparture(travelId, departureId, jwt.getClaim("sub"));
        return ResponseEntity.ok(response);
    }

    
    //BOOKING AREA

    @PostMapping("/{travelId}/reserve")
    public ResponseEntity<Void> reserveSpots(@PathVariable UUID travelId, @RequestParam int spots) {
        travelDepartureService.reserveSpots(travelId, spots);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{travelId}/release")
    public ResponseEntity<Void> releaseSpots(@PathVariable UUID travelId, @RequestParam int spots) {
        travelDepartureService.releaseSpots(travelId, spots);
        return ResponseEntity.ok().build();
    }


}
