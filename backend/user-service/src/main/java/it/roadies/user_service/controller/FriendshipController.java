package it.roadies.user_service.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.roadies.user_service.data.dto.response.FriendshipResponseDTO;
import it.roadies.user_service.data.dto.response.UserProfileResponseDTO;
import it.roadies.user_service.data.entities.enumeration.Status;
import it.roadies.user_service.services.FriendshipService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/friends")
@RequiredArgsConstructor
@Tag(name = "Friendship Management", description = "API per la completa gestione delle richieste di amicizia")
public class FriendshipController {

    private final FriendshipService friendshipService;

    @PreAuthorize("hasRole('TRAVELER')")
    @PostMapping("/request/{receiverUsername}")
    @Operation(summary = "Invia una richiesta", description = "Invia una richiesta di amicizia a un utente tramite il suo username.")
    @ApiResponse(responseCode = "200", description = "Richiesta inviata con successo")
    public ResponseEntity<Void> send(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String receiverUsername) {
        log.info("Ricevuta richiesta di amicizia dal subject JWT: {} verso lo username: {}", jwt.getSubject(), receiverUsername);
        friendshipService.sendRequest(jwt.getSubject(), receiverUsername);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('TRAVELER')")
    @PatchMapping("/respond/{friendshipId}")
    @Operation(summary = "Rispondi a una richiesta", description = "Accetta o rifiuta una richiesta di amicizia ricevuta.")
    public ResponseEntity<Void> respond(
            @PathVariable UUID friendshipId,
            @RequestParam Status status,
            @AuthenticationPrincipal Jwt jwt) {
        log.info("Ricevuta risposta ({}) alla richiesta di amicizia ID: {} dal subject JWT: {}", status, friendshipId, jwt.getSubject());
        friendshipService.respondToRequest(friendshipId, status, jwt.getSubject());
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('TRAVELER')")
    @GetMapping("/list")
    @Operation(summary = "Lista amici rapida", description = "Restituisce i profili base di tutti gli amici confermati.")
    public ResponseEntity<List<UserProfileResponseDTO>> getFriends(@AuthenticationPrincipal Jwt jwt) {
        log.info("Ricevuta richiesta di elenco amici base dal subject JWT: {}", jwt.getSubject());
        return ResponseEntity.ok(friendshipService.getFriendsList(jwt.getSubject()));
    }

    @PreAuthorize("hasRole('TRAVELER')")
    @GetMapping("/detailed-list")
    @Operation(summary = "Lista amici dettagliata", description = "Restituisce i profili degli amici con dettagli sulla data di inizio amicizia.")
    public ResponseEntity<List<FriendshipResponseDTO>> getDetailedFriends(@AuthenticationPrincipal Jwt jwt) {
        log.info("Ricevuta richiesta di elenco amici dettagliato dal subject JWT: {}", jwt.getSubject());
        return ResponseEntity.ok(friendshipService.getDetailedFriendsList(jwt.getSubject()));
    }

    @PreAuthorize("hasRole('TRAVELER')")
    @GetMapping("/requests/pending")
    @Operation(summary = "Richieste in sospeso", description = "Recupera le richieste di amicizia ricevute in attesa di risposta.")
    public ResponseEntity<List<FriendshipResponseDTO>> getPendingRequests(@AuthenticationPrincipal Jwt jwt) {
        log.info("Ricevuta richiesta di elenco richieste in sospeso dal subject JWT: {}", jwt.getSubject());
        return ResponseEntity.ok(friendshipService.getPendingRequests(jwt.getSubject()));
    }

    @PreAuthorize("hasRole('TRAVELER')")
    @DeleteMapping("/{friendshipId}")
    @Operation(summary = "Rimuovi un amico", description = "Elimina una relazione di amicizia esistente.")
    @ApiResponse(responseCode = "204", description = "Amicizia rimossa correttamente")
    public ResponseEntity<Void> removeFriend(
            @PathVariable UUID friendshipId,
            @AuthenticationPrincipal Jwt jwt) {
        log.info("Ricevuta richiesta di rimozione amicizia ID: {} dal subject JWT: {}", friendshipId, jwt.getSubject());
        friendshipService.removeFriend(friendshipId, jwt.getSubject());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/requests/sent")
    public ResponseEntity<List<FriendshipResponseDTO>> getSentRequests(
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        return ResponseEntity.ok(friendshipService.getSentRequests(userId));
    }
}