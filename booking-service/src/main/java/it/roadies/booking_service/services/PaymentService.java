package it.roadies.booking_service.services;

import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import it.roadies.booking_service.data.dto.request.PaymentRequest;
import it.roadies.booking_service.data.dto.response.PaymentResponse;


public interface PaymentService {
    PaymentResponse createPaymentIntent(PaymentRequest request, String userJwt) throws StripeException;

    void processStripeEvent(Event event);
}
