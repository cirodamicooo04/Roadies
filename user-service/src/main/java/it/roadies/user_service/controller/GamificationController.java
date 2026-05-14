package it.roadies.user_service.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.roadies.user_service.services.GamificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/gamification")
@RequiredArgsConstructor
@Tag(name = "Gamification Management", description = "API per la gestione dei punti e dei badge degli utenti")
public class GamificationController {

    private final GamificationService gamificationService;

    @PatchMapping("/purchase")
    @Operation(summary = "Aggiungi punti per acquisto", description = "Aggiunge punti al profilo gamification dell'utente loggato in base alla spesa effettuata")
    public ResponseEntity<String> addPurchase(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam double amount) {

        String keycloakId = jwt.getSubject();
        log.info("Ricevuta richiesta di aggiunta punti per acquisto dal subject JWT: {} per importo: {}€", keycloakId, amount);

        gamificationService.addPointsBySpending(keycloakId, amount);
        return ResponseEntity.ok("Punti aggiunti con successo per la spesa effettuata di: " + amount + "€");
    }
}