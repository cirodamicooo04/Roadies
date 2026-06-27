package it.roadies.travel_service.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.roadies.travel_service.data.dto.request.ActivityCreateRequest;
import it.roadies.travel_service.data.dto.request.ActivityDepartureCreateRequest;
import it.roadies.travel_service.data.dto.request.ActivityDepartureUpdateRequest;
import it.roadies.travel_service.data.dto.request.ActivityUpdateRequest;
import it.roadies.travel_service.data.dto.response.ActivityDepartureResponse;
import it.roadies.travel_service.data.dto.response.ActivityResponse;
import it.roadies.travel_service.data.dto.response.ActivityBatchResponse;
import it.roadies.travel_service.services.ActivityDepartureService;
import it.roadies.travel_service.services.ActivityService;
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
@RequestMapping("/api/v1/activities")
@RequiredArgsConstructor
@Tag(name = "Attività", description = "API per la gestione, consultazione e prenotazione delle attività")
public class ActivityController {

    private final ActivityService activityService;
    private final ActivityDepartureService activityDepartureService;

    @Operation(summary = "Crea attività", description = "Crea una nuova attività associata all'organizzatore autenticato.")
    @PreAuthorize("hasRole('ORGANIZER')")
    @PostMapping
    public ResponseEntity<ActivityResponse> createActivity(@RequestBody @Valid ActivityCreateRequest request, @AuthenticationPrincipal Jwt jwt){
        ActivityResponse response = activityService.createActivity(request, jwt.getClaim("sub"));
        return ResponseEntity.status(201).body(response);
    }

    @Operation(summary = "Dettaglio attività", description = "Recupera i dettagli pubblici di un'attività.")
    @GetMapping("/public/{id}")
    public ResponseEntity<ActivityResponse> getActivity(@PathVariable UUID id){
        ActivityResponse response = activityService.getActivityById(id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Elimina attività", description = "Elimina un'attività creata dall'organizzatore autenticato.")
    @PreAuthorize("hasRole('ORGANIZER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteActivity(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt){
        activityService.deleteActivityById(id, jwt.getClaim("sub"));
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Aggiorna attività", description = "Modifica i dati di un'attività creata dall'organizzatore autenticato.")
    @PreAuthorize("hasRole('ORGANIZER')")
    @PutMapping("/{id}")
    public ResponseEntity<ActivityResponse> updateActivity(@PathVariable UUID id, @RequestBody @Valid ActivityUpdateRequest request, @AuthenticationPrincipal Jwt jwt){
        ActivityResponse response = activityService.updateActivity(id,request,jwt.getClaim("sub"));
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Aggiungi partenza attività", description = "Aggiunge una nuova partenza a un'attività dell'organizzatore autenticato.")
    @PreAuthorize("hasRole('ORGANIZER')")
    @PostMapping("/{activityId}/departures")
    public ResponseEntity<ActivityDepartureResponse> addDeparture(@PathVariable UUID activityId, @RequestBody @Valid ActivityDepartureCreateRequest request, @AuthenticationPrincipal Jwt jwt){
        ActivityDepartureResponse response = activityService.addDeparture(activityId,request,jwt.getClaim("sub"));
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Elimina partenza attività", description = "Elimina una partenza da un'attività dell'organizzatore autenticato.")
    @PreAuthorize("hasRole('ORGANIZER')")
    @DeleteMapping("/{activityId}/departures/{departureId}")
    public ResponseEntity<?> deleteDeparture(@PathVariable UUID activityId, @PathVariable UUID departureId, @AuthenticationPrincipal Jwt jwt){
        activityService.deleteDeparture(activityId, departureId, jwt.getClaim("sub"));
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Aggiorna partenza attività", description = "Modifica una partenza di un'attività dell'organizzatore autenticato.")
    @PreAuthorize("hasRole('ORGANIZER')")
    @PutMapping("/{activityId}/departures/{departureId}")
    public ResponseEntity<ActivityDepartureResponse> updateDeparture(@PathVariable UUID activityId, @PathVariable UUID departureId, @RequestBody @Valid ActivityDepartureUpdateRequest request, @AuthenticationPrincipal Jwt jwt){
        ActivityDepartureResponse response = activityService.updateDeparture(activityId,departureId,request,jwt.getClaim("sub"));
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Lista partenze attività", description = "Recupera le partenze disponibili per un'attività.")
    @GetMapping("/public/{activityId}/departures")
    public ResponseEntity<List<ActivityDepartureResponse>> getDepartures(@PathVariable UUID activityId){
        List<ActivityDepartureResponse> responses = activityService.getDepartures(activityId);
        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "Conferma partenza attività", description = "Conferma una partenza di un'attività.")
    @PatchMapping("/{activityId}/departures/{departureId}/confirm")
    public ResponseEntity<ActivityDepartureResponse> confirmDeparture(@PathVariable UUID activityId, @PathVariable UUID departureId, @AuthenticationPrincipal Jwt jwt){
        ActivityDepartureResponse response = activityService.confirmDeparture(activityId,departureId,jwt.getClaim("sub"));
        return ResponseEntity.ok(response);
    }

    //BOOKING AREA

    @Operation(summary = "Ottieni dettagli attività in batch", description = "Restituisce i dettagli di una lista di attività.")
    @PostMapping("/batch")
    public ResponseEntity<List<ActivityBatchResponse>> getActivitiesBatch(@RequestBody List<UUID> activityIds) {
        List<ActivityBatchResponse> response = activityDepartureService.getActivitiesBatch(activityIds);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Verifica attività", description = "Verifica se un'attività è valida per il processo di prenotazione.")
    @GetMapping("/{activityId}")
    public ResponseEntity<Void> isValidActivity(@PathVariable UUID activityId) {
        if (activityDepartureService.isValidActivity(activityId)) {
            return ResponseEntity.ok().build();
        }
        else return ResponseEntity.notFound().build();
    }

    @Operation(summary = "Prezzo attività", description = "Recupera il prezzo dell'attività per il processo di prenotazione.")
    @GetMapping("/{activityId}/price")
    public ResponseEntity<BigDecimal> getActivityPrice(@PathVariable UUID activityId) {
        BigDecimal response = activityDepartureService.getActivityPriceById(activityId);
        return ResponseEntity.ok(response);
    }

    //REVIEW AREA
    @GetMapping("/review/{activityId}/")
    public ResponseEntity<Void> isValidActivityAndIsNotIntoATravel(@PathVariable UUID activityId) {
        if (activityService.isValidActivityAndIsNotIntoATravel(activityId)) {
            return ResponseEntity.ok().build();
        }
        else return ResponseEntity.notFound().build();
    }
}
