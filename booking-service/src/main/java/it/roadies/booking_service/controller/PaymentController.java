package it.roadies.booking_service.controller;

import it.roadies.booking_service.services.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;

    @PostMapping("/public/create_payment")
    public ResponseEntity<Void> createPayment() {
        paymentService.createPayment();
        return ResponseEntity.noContent().build();
    }
}
