package it.roadies.booking_service.controller;

import io.minio.errors.MinioException;
import it.roadies.booking_service.data.dto.request.MemberDocumentUpdateRequest;
import it.roadies.booking_service.services.BookingMemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/documents")
@RequiredArgsConstructor
public class BookingMemberController {

    private final BookingMemberService bookingMemberService;

//    @PreAuthorize("hasAnyRole('TRAVELER', 'ADMIN')")
//    @PatchMapping ("/update")
//    public ResponseEntity<Void> updateDocument(@Valid @RequestBody MemberDocumentUpdateRequest request) {
//        bookingMemberService.updateDocument(request);
//        return ResponseEntity.ok().build();
//    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping ("/accept")
    public ResponseEntity<Void> acceptDocument(@Valid @RequestBody MemberDocumentUpdateRequest request) {
        bookingMemberService.acceptDocument(request);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping ("/reject")
    public ResponseEntity<Void> rejectDocument(@Valid @RequestBody MemberDocumentUpdateRequest request) {
        bookingMemberService.rejectDocument(request);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasAnyRole('TRAVELER')")
    @PostMapping(value = "/{documentId}/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadDocumentPhoto(@PathVariable UUID documentId,  @RequestParam("file") MultipartFile file, @AuthenticationPrincipal Jwt userJwt) throws MinioException {

        String fileUrl = bookingMemberService.uploadDocumentPhoto(documentId, file, userJwt.getSubject());
        return ResponseEntity.ok(fileUrl);
    }
}
