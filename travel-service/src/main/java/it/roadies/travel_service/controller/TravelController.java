package it.roadies.travel_service.controller;

import it.roadies.travel_service.data.dto.request.TravelCreateRequest;
import it.roadies.travel_service.data.dto.response.TravelResponse;
import it.roadies.travel_service.services.TravelDepartureService;
import it.roadies.travel_service.services.TravelService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/travels")
@RequiredArgsConstructor
public class TravelController {

    private final TravelService travelService;
    private final TravelDepartureService travelDepartureService;

    @PreAuthorize("hasRole('ORGANIZER')")
    @PostMapping
    public ResponseEntity<TravelResponse> createTravel(@RequestBody TravelCreateRequest travelCreateRequest, @AuthenticationPrincipal Jwt jwt) {
        TravelResponse response =  travelService.createTravel(travelCreateRequest, jwt.getClaim("sub"));
        return ResponseEntity.status(201).body(response);
    }

    @GetMapping("/public/{id}")
    public ResponseEntity<TravelResponse> getTravelById(@PathVariable UUID id){
        TravelResponse response = travelService.getTravelById(id);
        return ResponseEntity.ok(response);
    }

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

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTravel(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        travelService.deleteTravelById(id, jwt.getClaim("sub"));
        return ResponseEntity.ok().build();
    }
}
