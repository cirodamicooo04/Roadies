package it.roadies.booking_service.exceptions;

import java.util.UUID;

public class BookingNotFoundException extends RuntimeException {
    public BookingNotFoundException(UUID id) {
        super("Booking non trovato con id: " + id);
    }
}
