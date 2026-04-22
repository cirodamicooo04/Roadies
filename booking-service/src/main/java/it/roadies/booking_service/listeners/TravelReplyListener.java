package it.roadies.booking_service.listeners;

import it.roadies.booking_service.data.dao.BookingRepository;
import it.roadies.booking_service.data.dto.event.SpotsFailedEvent;
import it.roadies.booking_service.data.dto.event.SpotsReservedEvent;
import it.roadies.booking_service.data.entities.Booking;
import it.roadies.booking_service.data.entities.enumeration.BookingStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class TravelReplyListener {
    private final BookingRepository bookingRepository;

    @Transactional
    @RabbitListener(queuesToDeclare = @Queue("spots.reserved.queue"))
    public void handleSpotsReserved(SpotsReservedEvent event) {
        Booking booking = bookingRepository.findById(event.getBookingId()).orElseThrow();

        booking.setStatus(BookingStatus.PENDING);
        booking.setExpiresAt(LocalDateTime.now().plusMinutes(15));
        bookingRepository.save(booking);
    }

    @Transactional
    @RabbitListener(queuesToDeclare = @Queue("spots.failed.queue"))
    public void handleSpotsFailed(SpotsFailedEvent event) {
        Booking booking = bookingRepository.findById(event.getBookingId()).orElseThrow();

        booking.setStatus(BookingStatus.REJECTED);
        bookingRepository.save(booking);
    }
}