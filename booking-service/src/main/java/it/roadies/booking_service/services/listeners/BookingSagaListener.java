package it.roadies.booking_service.services.listeners;

import it.roadies.booking_service.data.dao.BookingRepository;
import it.roadies.booking_service.data.dto.event.SeatReservationFailedEvent;
import it.roadies.booking_service.data.dto.event.SeatReservedEvent;
import it.roadies.booking_service.data.entities.enumeration.BookingStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class BookingSagaListener {
    private final BookingRepository bookingRepository;

    @RabbitListener(queues = "booking.reserved.queue")
    public void handleSeatReserved(SeatReservedEvent event) {
        bookingRepository.findById(event.getBookingId()).ifPresent(booking -> {
            if (booking.getStatus() != BookingStatus.CANCELLED) {
                booking.setStatus(BookingStatus.RESERVE_CONFIRMED);
                booking.setExpiresAt(LocalDateTime.now().plusMinutes(15));
                bookingRepository.save(booking);
            }
        });
    }

    @RabbitListener(queues = "booking.failed.queue")
    public void handleSeatReservationFailed(SeatReservationFailedEvent event) {
        bookingRepository.findById(event.getBookingId()).ifPresent(booking -> {
            booking.setStatus(BookingStatus.RESERVE_REJECTED);
            bookingRepository.save(booking);
        });
    }
}