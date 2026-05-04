package it.roadies.booking_service.controller;

import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.net.Webhook;
import it.roadies.booking_service.data.dto.request.PaymentRequest;
import it.roadies.booking_service.data.dto.response.PaymentResponse;
import it.roadies.booking_service.services.BookingService;
import it.roadies.booking_service.services.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService stripePaymentService;

    @Value("${stripe.webhook.secret}")
    private String endpointSecret;

    @PreAuthorize("hasRole('TRAVELER')")
    @PostMapping("/private/create-payment-intent")
    public ResponseEntity<PaymentResponse> createPaymentIntent(@RequestBody PaymentRequest request) throws StripeException {
        PaymentResponse response = stripePaymentService.createPaymentIntent(request);
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