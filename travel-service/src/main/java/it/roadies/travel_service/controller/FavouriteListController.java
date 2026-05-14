package it.roadies.travel_service.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.roadies.travel_service.data.dto.request.FavouriteListCreateRequest;
import it.roadies.travel_service.data.dto.response.FavouriteListResponse;
import it.roadies.travel_service.data.entity.FavouriteList;
import it.roadies.travel_service.data.mapper.FavouriteListMapper;
import it.roadies.travel_service.services.FavouriteListService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/favourite-lists")
@RequiredArgsConstructor
@Tag(name = "Favourite Lists Management", description = "API per la gestione delle liste preferiti dei viaggiatori")
public class FavouriteListController {

    private final FavouriteListService listService;
    private final FavouriteListMapper listMapper;

    // --- GESTIONE DELLA LISTA ---

    @PreAuthorize("hasRole('TRAVELER')")
    @PostMapping
    @Operation(summary = "Crea una nuova lista", description = "Crea una lista preferiti vuota con una visibilità specifica.")
    public ResponseEntity<FavouriteListResponse> createList(
            @RequestBody @Valid FavouriteListCreateRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        FavouriteList created = listService.createList(
                request.getName(),
                request.getVisibility(),
                jwt.getClaim("sub")
        );
        return ResponseEntity.status(201).body(listMapper.toResponse(created));
    }

    @PreAuthorize("hasRole('TRAVELER')")
    @GetMapping("/my-lists")
    @Operation(summary = "Le mie liste", description = "Recupera tutte le liste create dall'utente loggato.")
    public ResponseEntity<List<FavouriteListResponse>> getMyLists(@AuthenticationPrincipal Jwt jwt) {
        List<FavouriteList> lists = listService.getMyLists(jwt.getClaim("sub"));

        List<FavouriteListResponse> response = lists.stream()
                .map(listMapper::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('TRAVELER')")
    @GetMapping("/{id}")
    @Operation(summary = "Dettaglio lista", description = "Recupera una lista e i suoi elementi. Effettua controlli di sicurezza in base alla visibilità.")
    public ResponseEntity<FavouriteListResponse> getList(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt) {

        FavouriteList list = listService.getListWithPermissions(id, jwt.getClaim("sub"));
        return ResponseEntity.ok(listMapper.toResponse(list));
    }

    @PreAuthorize("hasRole('TRAVELER')")
    @DeleteMapping("/{id}")
    @Operation(summary = "Elimina lista", description = "Elimina definitivamente una lista e tutti i collegamenti al suo interno.")
    public ResponseEntity<Void> deleteList(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt) {

        listService.deleteList(id, jwt.getClaim("sub"));
        return ResponseEntity.noContent().build();
    }


    // --- GESTIONE DEGLI AMICI (SHARED_SPECIFIC) ---

    @PreAuthorize("hasRole('TRAVELER')")
    @PostMapping("/{listId}/friends/{friendId}")
    @Operation(summary = "Aggiungi amico alla lista", description = "Autorizza un amico specifico a vedere questa lista (utile per liste SHARED_SPECIFIC).")
    public ResponseEntity<Void> addFriendToList(
            @PathVariable UUID listId,
            @PathVariable String friendId,
            @AuthenticationPrincipal Jwt jwt) {

        listService.addFriendToList(listId, friendId, jwt.getClaim("sub"));
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('TRAVELER')")
    @DeleteMapping("/{listId}/friends/{friendId}")
    @Operation(summary = "Rimuovi amico dalla lista", description = "Revoca a un amico specifico l'autorizzazione a vedere questa lista.")
    public ResponseEntity<Void> removeFriendFromList(
            @PathVariable UUID listId,
            @PathVariable String friendId,
            @AuthenticationPrincipal Jwt jwt) {

        listService.removeFriendFromList(listId, friendId, jwt.getClaim("sub"));
        return ResponseEntity.noContent().build();
    }


    // --- GESTIONE DEI VIAGGI NELLA LISTA ---

    @PreAuthorize("hasRole('TRAVELER')")
    @PostMapping("/{listId}/travels/{travelId}")
    @Operation(summary = "Aggiungi viaggio", description = "Aggiunge un viaggio alla lista preferiti.")
    public ResponseEntity<Void> addTravelToList(
            @PathVariable UUID listId,
            @PathVariable UUID travelId,
            @AuthenticationPrincipal Jwt jwt) {

        listService.addTravelToList(listId, travelId, jwt.getClaim("sub"));
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('TRAVELER')")
    @DeleteMapping("/{listId}/travels/{travelId}")
    @Operation(summary = "Rimuovi viaggio", description = "Rimuove un viaggio dalla lista preferiti.")
    public ResponseEntity<Void> removeTravelFromList(
            @PathVariable UUID listId,
            @PathVariable UUID travelId,
            @AuthenticationPrincipal Jwt jwt) {

        listService.removeTravelFromList(listId, travelId, jwt.getClaim("sub"));
        return ResponseEntity.noContent().build();
    }


    @PreAuthorize("hasRole('TRAVELER')")
    @PostMapping("/{listId}/activities/{activityId}")
    @Operation(summary = "Aggiungi attività", description = "Aggiunge una singola attività alla lista preferiti.")
    public ResponseEntity<Void> addActivityToList(
            @PathVariable UUID listId,
            @PathVariable UUID activityId,
            @AuthenticationPrincipal Jwt jwt) {

        listService.addActivityToList(listId, activityId, jwt.getClaim("sub"));
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('TRAVELER')")
    @DeleteMapping("/{listId}/activities/{activityId}")
    @Operation(summary = "Rimuovi attività", description = "Rimuove una singola attività dalla lista preferiti.")
    public ResponseEntity<Void> removeActivityFromList(
            @PathVariable UUID listId,
            @PathVariable UUID activityId,
            @AuthenticationPrincipal Jwt jwt) {

        listService.removeActivityFromList(listId, activityId, jwt.getClaim("sub"));
        return ResponseEntity.noContent().build();
    }
}