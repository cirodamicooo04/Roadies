package it.roadies.booking_service.services;

import it.roadies.booking_service.data.dto.request.BookingCreateRequest;
import it.roadies.booking_service.data.dto.request.BookingDraftRequest;
import it.roadies.booking_service.data.dto.request.BookingMemberRequest;
import it.roadies.booking_service.data.dto.response.BookingDraftResponse;
import it.roadies.booking_service.data.dto.response.BookingStatusResponse;
import it.roadies.booking_service.data.dto.response.BookingStep2Response;
import it.roadies.booking_service.data.entities.Booking;

import java.util.List;
import java.util.UUID;


public interface BookingService {
    BookingDraftResponse createDraft(BookingDraftRequest b, String userJwt);
    void createPendingAndReserveSeats(BookingCreateRequest b, String userJwt);
    BookingStep2Response insertMembers(BookingMemberRequest b, String userJwt);
    BookingStatusResponse getBookingStatus(UUID bookingId, String userJwt);
    void confirmBookingAfterPayment(UUID bookingId);
    void deleteBooking(UUID bookingId, String userJwt, String mailTo);
    List<UUID> getUserBookings(String userId);
    void updateBookingIfAllDocumentsUploaded(Booking booking);
    void deleteMinioDocument(Booking booking);
}
