package it.roadies.booking_service.controller;

import it.roadies.booking_service.data.dto.MemberDocumentDTO;
import it.roadies.booking_service.data.dto.response.BookingDraftResponse;
import it.roadies.booking_service.services.BookingMemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/documents")
@RequiredArgsConstructor
public class BookingMemberController {

    private final BookingMemberService bookingMemberService;

    @PutMapping ("/update")
    public ResponseEntity<BookingDraftResponse> updateDocument(@Valid @RequestBody MemberDocumentDTO request) {
        bookingMemberService.updateDocument(request);
        return ResponseEntity.ok().build();
    }

    @PutMapping ("/accept")
    public ResponseEntity<BookingDraftResponse> acceptDocument(@Valid @RequestBody MemberDocumentDTO request) {
        bookingMemberService.acceptDocument(request);
        return ResponseEntity.ok().build();
    }

    @PutMapping ("/reject")
    public ResponseEntity<BookingDraftResponse> rejectDocument(@Valid @RequestBody MemberDocumentDTO request) {
        bookingMemberService.rejectDocument(request);
        return ResponseEntity.ok().build();
    }
}
