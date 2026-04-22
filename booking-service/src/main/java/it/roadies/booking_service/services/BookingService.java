package it.roadies.booking_service.services;

import it.roadies.booking_service.data.dto.request.BookingRequest;
import it.roadies.booking_service.data.dto.response.BookingResponse;


public interface BookingService {
    public BookingResponse createBooking(BookingRequest b);
}
