package it.roadies.travel_service.controller;

import it.roadies.travel_service.data.dto.request.ActivityCreateRequest;
import it.roadies.travel_service.data.dto.request.ActivityDepartureCreateRequest;
import it.roadies.travel_service.data.dto.request.ActivityDepartureUpdateRequest;
import it.roadies.travel_service.data.dto.response.ActivityDepartureResponse;
import it.roadies.travel_service.data.dto.response.ActivityResponse;
import it.roadies.travel_service.services.ActivityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/activities")
@RequiredArgsConstructor
public class ActivityController {

    private final ActivityService activityService;

    @PreAuthorize("hasRole('ORGANIZER')")
    @PostMapping
    public ResponseEntity<ActivityResponse> createActivity(@RequestBody @Valid ActivityCreateRequest request, @AuthenticationPrincipal Jwt jwt){
        ActivityResponse response = activityService.createActivity(request, jwt.getClaim("sub"));
        return ResponseEntity.status(201).body(response);
    }

    @GetMapping("/public/{id}")
    public ResponseEntity<ActivityResponse> getActivity(@PathVariable UUID id){
        ActivityResponse response = activityService.getActivityById(id);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ORGANIZER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteActivity(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt){
        activityService.deleteActivityById(id, jwt.getClaim("sub"));
        return ResponseEntity.ok().build();
    }

    //DA VEDERE PROBLEMA MAPPER AGGIORNAMENTO
//    @PreAuthorize("hasRole('ORGANIZER')")
//    @PutMapping("/{id}")
//    public ResponseEntity<ActivityResponse> updateActivity(@PathVariable UUID id, @RequestBody @Valid ActivityUpdateRequest request, @AuthenticationPrincipal Jwt jwt){
//        return null;
//    }

    @PreAuthorize("hasRole('ORGANIZER')")
    @PostMapping("/{activityId}/departures")
    public ResponseEntity<ActivityDepartureResponse> addDeparture(@PathVariable UUID activityId, @RequestBody @Valid ActivityDepartureCreateRequest request, @AuthenticationPrincipal Jwt jwt){
        ActivityDepartureResponse response = activityService.addDeparture(activityId,request,jwt.getClaim("sub"));
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ORGANIZER')")
    @DeleteMapping("/{activityId}/departures/{departureId}")
    public ResponseEntity<?> deleteDeparture(@PathVariable UUID activityId, @PathVariable UUID departureId, @AuthenticationPrincipal Jwt jwt){
        activityService.deleteDeparture(activityId, departureId, jwt.getClaim("sub"));
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('ORGANIZER')")
    @PutMapping("/{activityId}/departures/{departureId}")
    public ResponseEntity<ActivityDepartureResponse> updateDeparture(@PathVariable UUID activityId, @PathVariable UUID departureId, @RequestBody @Valid ActivityDepartureUpdateRequest request, @AuthenticationPrincipal Jwt jwt){
        ActivityDepartureResponse response = activityService.updateDeparture(activityId,departureId,request,jwt.getClaim("sub"));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/public/{activityId}/departures")
    public ResponseEntity<List<ActivityDepartureResponse>> getDepartures(@PathVariable UUID activityId){
        List<ActivityDepartureResponse> responses = activityService.getDepartures(activityId);
        return ResponseEntity.ok(responses);
    }

    @PatchMapping("/{activityId}/departures/{departureId}/confirm")
    public ResponseEntity<ActivityDepartureResponse> confirmDeparture(@PathVariable UUID activityId, @PathVariable UUID departureId, @AuthenticationPrincipal Jwt jwt){
        ActivityDepartureResponse response = activityService.confirmDeparture(activityId,departureId,jwt.getClaim("sub"));
        return ResponseEntity.ok(response);
    }
}
