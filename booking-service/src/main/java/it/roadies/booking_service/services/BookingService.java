package it.roadies.booking_service.services;

import it.roadies.booking_service.data.dto.request.BookingCreateRequest;
import it.roadies.booking_service.data.dto.request.BookingDraftRequest;
import it.roadies.booking_service.data.dto.request.BookingMemberRequest;
import it.roadies.booking_service.data.dto.response.BookingDraftResponse;
import it.roadies.booking_service.data.dto.response.BookingStatusResponse;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.UUID;


public interface BookingService {
    public BookingDraftResponse createDraft(BookingDraftRequest b);
    public void createBookingStep1(BookingCreateRequest b);
    public void createBookingStep2(BookingMemberRequest b);
    BookingStatusResponse getBookingStatus(UUID bookingId);
    public void confirmBooking(UUID bookingId);
    void deleteBooking(UUID bookingId);
    List<UUID> getUserBookings(String userId);
}
