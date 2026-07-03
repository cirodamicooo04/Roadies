package it.roadies.booking_service.services.listeners;

import it.roadies.booking_service.data.dao.BookingRepository;
import it.roadies.shared.contracts.SeatReservationFailedEvent;
import it.roadies.shared.contracts.SeatReservedEvent;
import it.roadies.booking_service.data.entities.enumeration.BookingStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingSagaListener {
    private final BookingRepository bookingRepository;
    private final RabbitTemplate rabbitTemplate;

    @Transactional
    @RabbitListener(queues = "booking.reserved.queue")
    public void handleSeatReserved(SeatReservedEvent event) {
        bookingRepository.findById(event.getBookingId()).ifPresent(booking -> {
            if (booking.getStatus() != BookingStatus.EXPIRED && booking.getStatus() != BookingStatus.CANCELLED) {
                booking.setStatus(BookingStatus.RESERVE_CONFIRMED);
                booking.setExpiresAt(LocalDateTime.now().plusMinutes(15));
                bookingRepository.save(booking);
                rabbitTemplate.convertAndSend("booking-delay-queue", booking.getId().toString());
            }
        });
    }

    @Transactional
    @RabbitListener(queues = "booking.failed.queue")
    public void handleSeatReservationFailed(SeatReservationFailedEvent event) {
        bookingRepository.findById(event.getBookingId()).ifPresent(booking -> {
            booking.setStatus(BookingStatus.RESERVE_REJECTED);
            bookingRepository.save(booking);
        });
    }
}