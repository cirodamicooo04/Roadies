package it.roadies.booking_service.controller;

import io.minio.errors.MinioException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.roadies.booking_service.services.BookingMemberService;
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
@RequestMapping("/api/v1/booking-documents")
@RequiredArgsConstructor
@Tag(name = "Gestione documenti", description = "API per la gestione dei documenti per ogni membro all'interno di una prenotazione")
public class BookingMemberController {

    private final BookingMemberService bookingMemberService;
    @Operation(summary = "Invia documenti", description = "Permette di inserire un documento relativo ad una prenotazione")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Documento inserito con successo"),
            @ApiResponse(responseCode = "401", description = "Utente non autenticato"),
            @ApiResponse(responseCode = "403", description = "Utente non autorizzato"),
    })
    @PreAuthorize("hasAnyRole('TRAVELER')")
    @PostMapping(value = "/{documentId}/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadDocumentPhoto(@PathVariable UUID documentId,  @RequestParam("file") MultipartFile file, @AuthenticationPrincipal Jwt userJwt) throws MinioException {

        String fileUrl = bookingMemberService.uploadDocumentPhoto(documentId, file, userJwt.getSubject());
        return ResponseEntity.ok(fileUrl);
    }
}
