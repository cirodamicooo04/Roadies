package it.roadies.user_service.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.roadies.user_service.data.dto.request.UserSyncRequestDTO;
import it.roadies.user_service.data.dto.response.UserProfileResponseDTO;
import it.roadies.user_service.data.dto.result.UserSyncResult;
import it.roadies.user_service.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

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
            @RequestBody UserSyncRequestDTO requestDto) {

        log.info("Ricevuta richiesta di sincronizzazione utente dal subject JWT: {}", jwt.getSubject());
        requestDto.setKeycloakId(jwt.getSubject());

        UserSyncResult result = userService.syncUser(requestDto);

        //Gestiamo due stati con 200 se l'utente esisteva e con 201 se l'utente non esisteva
        if (result.isNewUser()) {
            return ResponseEntity.status(HttpStatus.CREATED).body(result.profile());
        }

        return ResponseEntity.ok(result.profile());
    }

    @GetMapping("/me")
    @Operation(summary = "Il mio profilo", description = "Recupera i dati dell'utente loggato")
    public ResponseEntity<UserProfileResponseDTO> getMyProfile(@AuthenticationPrincipal Jwt jwt) {
        log.info("Ricevuta richiesta di recupero profilo personale dal subject JWT: {}", jwt.getSubject());
        return ResponseEntity.ok(userService.getProfile(jwt.getSubject()));
    }

    @GetMapping("/search/{username}")
    @Operation(summary = "Cerca utente", description = "Ricerca pubblica di un profilo")
    public ResponseEntity<UserProfileResponseDTO> searchUser(@PathVariable String username) {
        log.info("Ricevuta richiesta REST di ricerca utente per username: {}", username);
        return ResponseEntity.ok(userService.getProfileByUsername(username));
    }

    @PutMapping("/update")
    @Operation(summary = "Aggiorna profilo", description = "Modifica i dati del proprio profilo")
    public ResponseEntity<UserProfileResponseDTO> updateProfile(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody UserSyncRequestDTO updateDto) {
        log.info("Ricevuta richiesta di aggiornamento profilo dal subject JWT: {}", jwt.getSubject());
        return ResponseEntity.ok(userService.updateProfile(jwt.getSubject(), updateDto));
    }

    @DeleteMapping("/delete")
    @Operation(summary = "Elimina profilo", description = "Rimozione definitiva dell'account")
    public ResponseEntity<Void> deleteProfile(@AuthenticationPrincipal Jwt jwt) {
        log.info("Ricevuta richiesta di eliminazione profilo dal subject JWT: {}", jwt.getSubject());
        userService.deleteProfile(jwt.getSubject());
        return ResponseEntity.noContent().build();
    }
}