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
import org.springframework.web.bind.annotation.*;

import java.util.UUID;


@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
public class BookingController {
    private final BookingService bookingService;

    @PostMapping("/public/create_draft")
    public ResponseEntity<BookingDraftResponse> createDraftBooking(@Valid @RequestBody BookingDraftRequest request) {
        BookingDraftResponse response = bookingService.createDraft(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/public/create_pending")
    public ResponseEntity<Void> createBooking(@Valid @RequestBody BookingCreateRequest request) {
        bookingService.createBookingStep1(request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/public/create_members")
    public ResponseEntity<Void> createMembers(@Valid @RequestBody BookingMemberRequest request) {
        bookingService.createBookingStep2(request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{bookingId}/status")
    public ResponseEntity<BookingStatusResponse> getStatus(@PathVariable UUID bookingId) {
        return ResponseEntity.ok(bookingService.getBookingStatus(bookingId));
    }
}
