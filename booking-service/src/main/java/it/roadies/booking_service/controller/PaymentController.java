package it.roadies.booking_service.controller;

import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
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
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService stripePaymentService;
    private final BookingService bookingService;

    @Value("${stripe.webhook.secret}")
    private String endpointSecret;

    @PostMapping("/private/create-payment-intent")
    public ResponseEntity<PaymentResponse> createPaymentIntent(@RequestBody PaymentRequest request) throws StripeException {
        PaymentResponse response = stripePaymentService.createPaymentIntent(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/public/webhook")
    public ResponseEntity<String> handleStripeWebhook(@RequestBody String payload, @RequestHeader("Stripe-Signature") String sigHeader) {
        Event event;
        try {
            event = Webhook.constructEvent(payload, sigHeader, endpointSecret);
        } catch (SignatureVerificationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Firma non valida");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Errore di elaborazione");
        }

        if ("payment_intent.succeeded".equals(event.getType())) {
            PaymentIntent paymentIntent = (PaymentIntent) event.getDataObjectDeserializer().getObject().orElse(null);

            if (paymentIntent != null) {
                String bookingIdd = paymentIntent.getMetadata().get("bookingId");
                if (bookingIdd != null) {
                    try {
                        UUID bookingId = UUID.fromString(bookingIdd);
                        bookingService.confirmBooking(bookingId);

                    } catch (Exception e) {
                        //qui loggerò
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
                    }
                }
            }
        } else if ("payment_intent.payment_failed".equals(event.getType())) {
            //qui posso loggare e/o cancellare booking
        }
        return ResponseEntity.ok("Success");
    }
}