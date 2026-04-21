package it.roadies.booking_service.controller;

import it.roadies.booking_service.data.dto.request.BookingRequestDTO;
import it.roadies.booking_service.data.dto.response.BookingResponseDTO;
import it.roadies.booking_service.services.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
public class BookingController {
    private final BookingService bookingService;

    @PostMapping("/public/create/booking")
    public ResponseEntity<BookingResponseDTO> createBooking(@RequestBody BookingRequestDTO requestDto) {
        BookingResponseDTO response = bookingService.createBooking(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
