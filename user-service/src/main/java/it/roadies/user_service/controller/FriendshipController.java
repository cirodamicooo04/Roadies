package it.roadies.user_service.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.roadies.user_service.data.dto.response.FriendshipResponseDTO;
import it.roadies.user_service.data.dto.response.UserProfileResponseDTO;
import it.roadies.user_service.data.entities.enumeration.Status;
import it.roadies.user_service.services.FriendshipService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/friends")
@RequiredArgsConstructor
@Tag(name = "Friendship Management", description = "API per la completa gestione delle richieste di amicizia")
public class FriendshipController {

    private final FriendshipService friendshipService;

    @PostMapping("/request/{receiverUsername}")
    @Operation(summary = "Invia una richiesta", description = "Invia una richiesta di amicizia a un utente tramite il suo username.")
    @ApiResponse(responseCode = "200", description = "Richiesta inviata con successo")
    public ResponseEntity<Void> send(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String receiverUsername) {
        String myId = jwt.getSubject();
        friendshipService.sendRequest(myId, receiverUsername);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/respond/{friendshipId}")
    @Operation(summary = "Rispondi a una richiesta", description = "Accetta o rifiuta una richiesta di amicizia ricevuta.")
    public ResponseEntity<Void> respond(
            @PathVariable UUID friendshipId,
            @RequestParam Status status,
            @AuthenticationPrincipal Jwt jwt) {
        String myId = jwt.getSubject();
        friendshipService.respondToRequest(friendshipId, status, myId);
        return ResponseEntity.ok().build();
    }

    //Lista di amicizia rapida
    @GetMapping("/list")
    @Operation(summary = "Lista amici rapida", description = "Restituisce i profili base di tutti gli amici confermati.")
    public ResponseEntity<List<UserProfileResponseDTO>> getFriends(@AuthenticationPrincipal Jwt jwt) {
        String myId = jwt.getSubject();
        List<UserProfileResponseDTO> friends = friendshipService.getFriendsList(myId);
        return ResponseEntity.ok(friends);
    }

    // Lista amici DETTAGLIATA (come dicevamo all'interno della repository avrà l'account completo dell'amico, l'id dell'amicizia
    // e anche la data dell'inizio dell'amicizia
    @GetMapping("/detailed-list")
    @Operation(summary = "Lista amici dettagliata", description = "Restituisce i profili degli amici con dettagli sulla data di inizio amicizia.")
    public ResponseEntity<List<FriendshipResponseDTO>> getDetailedFriends(@AuthenticationPrincipal Jwt jwt) {
        String myId = jwt.getSubject();
        return ResponseEntity.ok(friendshipService.getDetailedFriendsList(myId));
    }

    @GetMapping("/requests/pending")
    @Operation(summary = "Richieste in sospeso", description = "Recupera le richieste di amicizia ricevute in attesa di risposta.")
    public ResponseEntity<List<FriendshipResponseDTO>> getPendingRequests(@AuthenticationPrincipal Jwt jwt) {
        String myId = jwt.getSubject();
        return ResponseEntity.ok(friendshipService.getPendingRequests(myId));
    }

    @DeleteMapping("/{friendshipId}")
    @Operation(summary = "Rimuovi un amico", description = "Elimina una relazione di amicizia esistente.")
    @ApiResponse(responseCode = "204", description = "Amicizia rimossa correttamente")
    public ResponseEntity<Void> removeFriend(
            @PathVariable UUID friendshipId,
            @AuthenticationPrincipal Jwt jwt) {
        String myId = jwt.getSubject();
        friendshipService.removeFriend(friendshipId, myId);
        return ResponseEntity.noContent().build();
    }
}