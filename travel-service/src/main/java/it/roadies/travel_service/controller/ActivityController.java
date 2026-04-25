package it.roadies.travel_service.controller;

import it.roadies.travel_service.data.dto.request.ActivityCreateRequest;
import it.roadies.travel_service.data.dto.response.ActivityResponse;
import it.roadies.travel_service.services.ActivityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

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


}
