package it.roadies.user_service.controller;

import io.swagger.v3.oas.annotations.Operation;
import it.roadies.user_service.services.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseEntity<Void> blockUser(String keycloakId) {
        adminService.blockUser(keycloakId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Sblocca un utente",
            description = "Permette ad un admin di sbloccare un utente specificato tramite il suo ID Keycloak. Un utente sbloccato potrà accedere nuovamente al sistema."
    )
    @PutMapping("users/{id}/unblock")
    public ResponseEntity<Void> unblockUser(String keycloakId) {
        adminService.unblockUser(keycloakId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Degrada un organizzatore a utente",
            description = "Permette ad un admin di degradare un organizzatore a utente normale specificato tramite il suo ID Keycloak. Un organizzatore degradato perderà i privilegi di organizzatore e tornerà ad essere un utente standard."
    )
    @PutMapping("users/{id}/demote")
    public ResponseEntity<Void> demoteOrganizerToUser(String keycloakId) {
        adminService.demoteOrganizerToUser(keycloakId);
        return ResponseEntity.noContent().build();
    }
}
