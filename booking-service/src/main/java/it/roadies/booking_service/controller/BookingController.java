package it.roadies.booking_service.controller;

import it.roadies.booking_service.data.dto.request.BookingCreateRequest;
import it.roadies.booking_service.data.dto.request.BookingDraftRequest;
import it.roadies.booking_service.data.dto.request.BookingMemberRequest;
import it.roadies.booking_service.data.dto.response.BookingDraftResponse;
import it.roadies.booking_service.data.dto.response.BookingStatusResponse;
import it.roadies.booking_service.services.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;


@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
public class BookingController {
    private final BookingService bookingService;

    @PreAuthorize("hasRole('TRAVELER')")
    @PostMapping("/private/create-draft")
    public ResponseEntity<BookingDraftResponse> createDraftBooking(@Valid @RequestBody BookingDraftRequest request) {
        BookingDraftResponse response = bookingService.createDraft(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PreAuthorize("hasRole('TRAVELER')")
    @PostMapping("/private/create-pending")
    public ResponseEntity<Void> createBooking(@Valid @RequestBody BookingCreateRequest request) {
        bookingService.createBookingStep1(request);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('TRAVELER')")
    @PostMapping("/private/create-members")
    public ResponseEntity<Void> createMembers(@Valid @RequestBody BookingMemberRequest request) {
        bookingService.createBookingStep2(request);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('TRAVELER')")
    @GetMapping("/{bookingId}/status")
    public ResponseEntity<BookingStatusResponse> getStatus(@PathVariable UUID bookingId) {
        return ResponseEntity.ok(bookingService.getBookingStatus(bookingId));
    }

    @PreAuthorize("hasRole('TRAVELER')")
    @PostMapping("/{bookingId}/confirm")
    public ResponseEntity<Void> confirmBooking(@PathVariable UUID bookingId) {
        bookingService.confirmBooking(bookingId);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('TRAVELER')")
    @DeleteMapping("/{bookingId}/delete")
    public ResponseEntity<Void> deleteBooking(@PathVariable UUID bookingId) {
        bookingService.deleteBooking(bookingId);
        return ResponseEntity.noContent().build();
    }
}
