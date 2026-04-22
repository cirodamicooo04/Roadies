package it.roadies.user_service.controller;

import it.roadies.user_service.services.GamificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/gamification")
@RequiredArgsConstructor
public class GamificationController {

    private final GamificationService gamificationService;

    @PatchMapping("/purchase/{keycloakId}")
    public ResponseEntity<String> addPurchase(@PathVariable String keycloakId, @RequestParam double amount) {
        gamificationService.addPointsBySpending(keycloakId, amount);
        return ResponseEntity.ok("Punti aggiunti con successo per la spesa effettuata di: " + amount + "€");
    }

}