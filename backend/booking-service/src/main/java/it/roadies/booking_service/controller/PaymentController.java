package it.roadies.booking_service.controller;

import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.net.Webhook;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.roadies.booking_service.data.dto.request.PaymentRequest;
import it.roadies.booking_service.data.dto.response.PaymentResponse;
import it.roadies.booking_service.services.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Tag(name = "Gestione Pagamento", description = "API per la creazione e la conferma di una richiesta di pagamento")
public class PaymentController {

    private final PaymentService stripePaymentService;

    @Value("${stripe.webhook.secret}")
    private String endpointSecret;

    @Operation(summary = "Crea richiesta pagamento", description = "Permette di creare una richiesta di pagamento")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Richiesta di pagamento effettuata con successo"),
            @ApiResponse(responseCode = "401", description = "Utente non autenticato"),
            @ApiResponse(responseCode = "403", description = "Utente non autorizzato"),
    })
    @PreAuthorize("hasRole('TRAVELER')")
    @PostMapping("/create-payment-intent")
    public ResponseEntity<PaymentResponse> createPaymentIntent(@Valid @RequestBody PaymentRequest request, @AuthenticationPrincipal Jwt userJwt) throws StripeException {
        PaymentResponse response = stripePaymentService.createPaymentIntent(request, userJwt.getSubject());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/public/webhook")
    public ResponseEntity<String> handleStripeWebhook(@RequestBody String payload, @RequestHeader("Stripe-Signature") String sigHeader) {
        try {
            Event event = Webhook.constructEvent(payload, sigHeader, endpointSecret);
            stripePaymentService.processStripeEvent(event);
            return ResponseEntity.ok("Success");

        } catch (SignatureVerificationException e) {
            log.error("Firma webhook Stripe non valida", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Firma non valida");
        } catch (Exception e) {
            log.error("Errore imprevisto durante l'elaborazione del webhook Stripe", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Errore server");
        }
    }
}