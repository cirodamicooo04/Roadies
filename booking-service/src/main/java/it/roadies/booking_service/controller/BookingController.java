package it.roadies.booking_service.controller;

import it.roadies.booking_service.data.dto.request.BookingCreateRequest;
import it.roadies.booking_service.data.dto.request.BookingDraftRequest;
import it.roadies.booking_service.data.dto.request.BookingMemberRequest;
import it.roadies.booking_service.data.dto.response.BookingDraftResponse;
import it.roadies.booking_service.data.dto.response.BookingStatusResponse;
import it.roadies.booking_service.data.dto.response.BookingStep2Response;
import it.roadies.booking_service.services.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
public class BookingController {
    private final BookingService bookingService;

    @PreAuthorize("hasRole('TRAVELER')")
    @PostMapping("/create-draft")
    public ResponseEntity<BookingDraftResponse> createDraftBooking(@Valid @RequestBody BookingDraftRequest request, @AuthenticationPrincipal Jwt userJwt) {
        BookingDraftResponse response = bookingService.createDraft(request, userJwt.getSubject());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PreAuthorize("hasRole('TRAVELER')")
    @PutMapping("/create-pending")
    public ResponseEntity<Void> createBooking(@Valid @RequestBody BookingCreateRequest request, @AuthenticationPrincipal Jwt userJwt) {
        bookingService.createBookingStep1(request, userJwt.getSubject());
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('TRAVELER')")
    @PostMapping("/create-members")
    public ResponseEntity<BookingStep2Response> createMembers(@Valid @RequestBody BookingMemberRequest request, @AuthenticationPrincipal Jwt userJwt) {
        BookingStep2Response response = bookingService.createBookingStep2(request, userJwt.getSubject());
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('TRAVELER')")
    @GetMapping("/{bookingId}/status")
    public ResponseEntity<BookingStatusResponse> getStatus(@PathVariable UUID bookingId, @AuthenticationPrincipal Jwt userJwt) {
        return ResponseEntity.ok(bookingService.getBookingStatus(bookingId, userJwt.getSubject()));
    }

//    @PreAuthorize("hasRole('TRAVELER')")
//    @PatchMapping("/{bookingId}/confirm")
//    public ResponseEntity<Void> confirmBooking(@PathVariable UUID bookingId) {
//        bookingService.confirmBookingAfterPayment(bookingId);
//        return ResponseEntity.noContent().build();
//    }

    @DeleteMapping("/{bookingId}")
    public ResponseEntity<Void> deleteBooking(@PathVariable UUID bookingId, @AuthenticationPrincipal Jwt userJwt) {
        bookingService.deleteBooking(bookingId, userJwt.getSubject());
        return ResponseEntity.noContent().build();
    }

    //TRAVEL SERVICE RECOMMENDATION
    @PreAuthorize("hasRole('TRAVELER')")
    @GetMapping("/users/me")
    public ResponseEntity<List<UUID>> getBookingsFromUser(@AuthenticationPrincipal Jwt jwt){
        List<UUID> travelsIds = bookingService.getUserBookings(jwt.getClaim("sub"));
        return ResponseEntity.ok(travelsIds);
    }
}
