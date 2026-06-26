package it.roadies.booking_service.services.listeners;

import it.roadies.booking_service.data.dao.BookingRepository;
import it.roadies.booking_service.data.dto.event.ReserveSeatCommand;
import it.roadies.booking_service.data.entities.enumeration.BookingStatus;
import it.roadies.booking_service.services.BookingService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingExpirationListener {
    private final BookingRepository bookingRepository;
    private final RabbitTemplate rabbitTemplate;
    private final BookingService bookingService;

    @Transactional
    @RabbitListener(queues = "booking-expiration-queue")
    public void processExpiredBooking(String bookingIdStr) {
        UUID bookingId = UUID.fromString(bookingIdStr);
        log.info("Timer scaduto! Controllo lo stato del booking ID: {}", bookingId);

        bookingRepository.findById(bookingId).ifPresent(booking -> {
            if ((booking.getStatus() == BookingStatus.RESERVE_CONFIRMED || booking.getStatus() == BookingStatus.READY_FOR_PAYMENT) && booking.getExpiresAt().isBefore(LocalDateTime.now())) {
                log.warn("Il booking {} è ancora RESERVE_CONFIRMED. Lo annullo e libero i posti.", bookingId);

                booking.setStatus(BookingStatus.EXPIRED);
                bookingService.deleteMinioDocument(booking);
                bookingRepository.save(booking);

                if (booking.getActivityId()!=null){
                    rabbitTemplate.convertAndSend("booking.exchange", "booking.seat.release.activity", new ReserveSeatCommand(bookingId, null, booking.getActivityId(), booking.getPeopleCount()));
                }

                else rabbitTemplate.convertAndSend("booking.exchange", "booking.seat.release.travel", new ReserveSeatCommand(bookingId, booking.getTravelId(), null, booking.getPeopleCount()));


            } else {
                log.info("Il booking {} ha cambiato stato (ora è {}). Nessuna azione necessaria.",
                        bookingId, booking.getStatus());
            }
        });
    }
}
