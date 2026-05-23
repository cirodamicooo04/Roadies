package it.roadies.booking_service.services.impl;

import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import it.roadies.booking_service.config.i8n.MessageLang;
import it.roadies.booking_service.data.dao.BookingRepository;
import it.roadies.booking_service.data.dto.request.PaymentRequest;
import it.roadies.booking_service.data.dto.response.PaymentResponse;
import it.roadies.booking_service.data.entities.Booking;
import it.roadies.booking_service.data.entities.enumeration.BookingStatus;
import it.roadies.booking_service.exceptions.AccessDeniedException;
import it.roadies.booking_service.exceptions.BookingNotFoundException;
import it.roadies.booking_service.exceptions.StatusException;
import it.roadies.booking_service.services.BookingService;
import it.roadies.booking_service.services.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
    private final BookingService bookingService;
    private final BookingRepository bookingRepository;
    private final MessageLang messageLang;

    @Override
    public void processStripeEvent(Event event) {
        if ("payment_intent.succeeded".equals(event.getType())) {
            PaymentIntent paymentIntent = (PaymentIntent) event.getDataObjectDeserializer().getObject().orElse(null);

            if (paymentIntent != null && paymentIntent.getMetadata().containsKey("bookingId")) {
                String bookingIdStr = paymentIntent.getMetadata().get("bookingId");
                try {
                    UUID bookingId = UUID.fromString(bookingIdStr);
                    bookingService.confirmBookingAfterPayment(bookingId);
                    log.info("Pagamento Stripe riuscito. Prenotazione {} confermata.", bookingId);
                } catch (IllegalArgumentException e) {
                    log.error("Il bookingId ricevuto da Stripe non è un UUID valido: {}", bookingIdStr, e);
                }
            } else {
                log.warn("Ricevuto webhook di successo da Stripe, ma nessun bookingId nei metadati. Evento ID: {}", event.getId());
            }

        } else if ("payment_intent.payment_failed".equals(event.getType())) {
            log.warn("Pagamento Stripe fallito per l'evento: {}", event.getId());
        }
    }

    @Override
    public PaymentResponse createPaymentIntent(PaymentRequest request, String userJwt) throws StripeException {
        Booking booking = bookingRepository.findById(request.getBookingId()).orElseThrow(() -> new BookingNotFoundException(messageLang.getMessage("error.booking.not.found", request.getBookingId())));
        if (!booking.getUserId().equals(userJwt)){
            throw new AccessDeniedException(messageLang.getMessage("error.access.denied"));
        }

        if (!booking.getStatus().equals(BookingStatus.READY_FOR_PAYMENT)) {
            throw new StatusException(messageLang.getMessage("error.status.payment.reserve"));
        }

        if (booking.getExpiresAt() == null || booking.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new StatusException(messageLang.getMessage("error.status.time"));
        }

        if (booking.getTotalPrice() == null || booking.getTotalPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new StatusException(messageLang.getMessage("error.status.payment.price"));
        }

        log.info("Creazione PaymentIntent per bookingId={}, peopleCount={}, totalPrice={}", booking.getId(), booking.getPeopleCount(), booking.getTotalPrice());

        PaymentIntentCreateParams params =
                PaymentIntentCreateParams.builder()
                        .setAmount(booking.getTotalPrice().multiply(BigDecimal.valueOf(100)).setScale(0, RoundingMode.HALF_UP).longValue())
                        .setCurrency("eur")
                        .putMetadata("bookingId", booking.getId().toString())
                        .setAutomaticPaymentMethods(
                                PaymentIntentCreateParams.AutomaticPaymentMethods
                                        .builder()
                                        .setEnabled(true)
                                        .build()
                        )
                        .build();

        PaymentIntent paymentIntent = PaymentIntent.create(params);
        return new PaymentResponse(paymentIntent.getId(), paymentIntent.getClientSecret(), paymentIntent.getStatus());
    }

}