package it.roadies.user_service.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.roadies.user_service.data.dto.request.UserSyncRequestDTO;
import it.roadies.user_service.data.dto.request.UserUpdateRequestDTO;
import it.roadies.user_service.data.dto.response.MinimalInformationResponseDTO;
import it.roadies.user_service.data.dto.response.PendingOrganizerRequestResponseDTO;
import it.roadies.user_service.data.dto.response.UserProfileResponseDTO;
import it.roadies.user_service.data.dto.result.UserSyncResult;
import it.roadies.user_service.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "API per la gestione del profilo utente e sincronizzazione")
public class UserController {

    private final UserService userService;

    @PostMapping("/sync")
    @Operation(summary = "Sincronizza Utente", description = "Crea o aggiorna il profilo dell'utente al login")
    public ResponseEntity<UserProfileResponseDTO> syncUser(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody UserSyncRequestDTO requestDto) {

        log.info("Ricevuta richiesta di sincronizzazione utente dal subject JWT: {}", jwt.getSubject());
        requestDto.setKeycloakId(jwt.getSubject());

        boolean isOrganizer = false;
        java.util.Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
        if (realmAccess != null && realmAccess.get("roles") != null) {
            isOrganizer = ((java.util.Collection<String>) realmAccess.get("roles")).contains("ORGANIZER");
        }

        UserSyncResult result = userService.syncUser(requestDto, isOrganizer);

        //Gestiamo due stati con 200 se l'utente esisteva e con 201 se l'utente non esisteva
        if (result.isNewUser()) {
            return ResponseEntity.status(HttpStatus.CREATED).body(result.profile());
        }

        return ResponseEntity.ok(result.profile());
    }

    @GetMapping("/me")
    @Operation(summary = "Il mio profilo", description = "Recupera i dati dell'utente loggato")
    public ResponseEntity<UserProfileResponseDTO> getProfile(@AuthenticationPrincipal Jwt jwt) {
        log.info("Ricevuta richiesta di recupero profilo personale dal subject JWT: {}", jwt.getSubject());
        return ResponseEntity.ok(userService.getProfile(jwt.getSubject()));
    }

    @GetMapping("/search")
    @Operation(summary = "Cerca utente", description = "Ricerca pubblica di un profilo")
    public ResponseEntity<List<UserProfileResponseDTO>> searchUsers(
            @RequestParam("username") String username
    ) {
        List<UserProfileResponseDTO> results = userService.getProfileByUsername(username);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/id/{username}")
    public ResponseEntity<String> getUserIdByUsername(@PathVariable String username) {
        String userId = userService.findIdByUsername(username);
        return userId != null ? ResponseEntity.ok(userId) : ResponseEntity.notFound().build();
    }

    @PutMapping("/update")
    @Operation(summary = "Aggiorna profilo", description = "Modifica i dati del proprio profilo")
    public ResponseEntity<UserProfileResponseDTO> updateProfile(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody UserUpdateRequestDTO updateDto) {
        log.info("Ricevuta richiesta di aggiornamento profilo dal subject JWT: {}", jwt.getSubject());
        return ResponseEntity.ok(userService.updateProfile(jwt.getSubject(), updateDto));
    }

    @PostMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserProfileResponseDTO> uploadAvatar(
            @RequestPart("avatarFile") MultipartFile avatarFile,
            Authentication authentication
    ) {
        String keycloakId = authentication.getName();
        return ResponseEntity.ok(userService.uploadAvatar(keycloakId, avatarFile));
    }

    @PreAuthorize("hasRole('TRAVELER')")
    @PostMapping("/request-organizer")
    @Operation(summary = "Richiedi ruolo organizzatore")
    public ResponseEntity<Void> requestOrganizerRole(@AuthenticationPrincipal Jwt jwt) {
        userService.requestOrganizerRole(jwt.getSubject());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/public/minimal-info")
    @Operation(summary = "Recupera info minime", description = "Restituisce ID, username e avatar per una lista di ID")
    public ResponseEntity<List<MinimalInformationResponseDTO>> getMinimalInformation(
            @RequestBody List<String> userIds) {

        return ResponseEntity.ok(userService.getMinimalInformation(userIds));
    }

    @GetMapping("/public/{username}/minimal-info")
    @Operation(summary = "Recupera info minime utente", description = "Restituisce ID, username e avatar per un singolo utente tramite username")
    public ResponseEntity<MinimalInformationResponseDTO> getUserMinimalInformation(@PathVariable String username){
        return ResponseEntity.ok(userService.getUserMinimalInformation(username));
    }

    @GetMapping("/{username}/is-organizer")
    public ResponseEntity<Boolean> checkIsOrganizer(@PathVariable String username) {
        boolean isOrganizer = userService.isUserOrganizer(username);
        return ResponseEntity.ok(isOrganizer);
    }
}