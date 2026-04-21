package it.roadies.booking_service.services;

import it.roadies.booking_service.data.dto.request.BookingRequestDTO;
import it.roadies.booking_service.data.dto.response.BookingResponseDTO;


public interface BookingService {
    public BookingResponseDTO createBooking(BookingRequestDTO b);
}
