package it.roadies.booking_service.data.dao;

import it.roadies.booking_service.data.entities.Booking;
import it.roadies.booking_service.data.entities.enumeration.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface BookingRepository extends JpaRepository<Booking, UUID> {
    List<Booking> findByStatusAndExpiresAtBefore(BookingStatus bookingStatus, LocalDateTime now);
}
