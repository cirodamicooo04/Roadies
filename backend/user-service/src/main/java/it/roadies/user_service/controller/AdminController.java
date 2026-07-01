package it.roadies.user_service.controller;

import io.swagger.v3.oas.annotations.Operation;
import it.roadies.user_service.data.dto.response.PendingOrganizerRequestResponseDTO;
import it.roadies.user_service.data.dto.response.UserResponseDTO;
import it.roadies.user_service.services.AdminService;
import jakarta.ws.rs.Path;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    @Operation(
            summary = "Blocca un utente",
            description = "Permette ad un admin di bloccare un utente specificato tramite il suo ID Keycloak. Un utente bloccato non potrà accedere al sistema finché non viene sbloccato."
    )
    @PutMapping("users/{id}/block")
    public ResponseEntity<Void> blockUser(@PathVariable String id) {
        adminService.blockUser(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Sblocca un utente",
            description = "Permette ad un admin di sbloccare un utente specificato tramite il suo ID Keycloak. Un utente sbloccato potrà accedere nuovamente al sistema."
    )
    @PutMapping("users/{id}/unblock")
    public ResponseEntity<Void> unblockUser(@PathVariable String id) {
        adminService.unblockUser(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/review-organizer/{targetUserId}")
    @Operation(summary = "Approva o rifiuta richiesta organizzatore")
    public ResponseEntity<Void> reviewOrganizerRequest(
            @PathVariable String targetUserId,
            @RequestParam boolean approved,
            @RequestParam(required = false) String reason) {
        adminService.reviewOrganizerRequest(targetUserId, approved, reason);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/organizer-requests/pending")
    @Operation(summary = "Lista richieste organizzatore in sospeso")
    public ResponseEntity<List<PendingOrganizerRequestResponseDTO>> getPendingOrganizerRequests() {
        return ResponseEntity.ok(adminService.getPendingOrganizerRequests());
    }


    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("users")
    @Operation(summary = "Recupera utenti filtrati", description = "Recupera una lista di utenti filtrati in base allo stato specificato. " +
            "Se non viene fornito alcun filtro, il valore predefinito è 'ACTIVE'.")
    public ResponseEntity<List<UserResponseDTO>> getFilteredUsers(
            @RequestParam(defaultValue = "ACTIVE") String filter) { // Default is ACTIVE if no filter is provided

        List<UserResponseDTO> users = adminService.getUsersByFilter(filter);
        return ResponseEntity.ok(users);
    }
}
