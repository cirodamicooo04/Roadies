package it.roadies.user_service.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.roadies.user_service.data.dto.request.UserDocumentRequestDTO;
import it.roadies.user_service.data.dto.response.UserDocumentResponseDTO;
import it.roadies.user_service.services.UserDocumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/documents")
@RequiredArgsConstructor
@Tag(name = "Document Management", description = "API per l'upload e la verifica dei documenti degli utenti")
public class UserDocumentController {
    private final UserDocumentService userDocumentService;

    @PreAuthorize("hasRole('TRAVELER')")
    @PostMapping(value = "/upload/{userId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Carica documento", description = "Carica un documento. Solo l'utente stesso può farlo.")
    public ResponseEntity<UserDocumentResponseDTO> upload(
            @PathVariable String userId,
            @RequestPart("document") UserDocumentRequestDTO dto,
            @RequestPart("file") MultipartFile file,
            @AuthenticationPrincipal Jwt jwt) {
        log.info("Ricevuta richiesta di upload documento per l'utente ID: {} con nome file: {}", userId, file.getOriginalFilename());
        return ResponseEntity.ok(userDocumentService.uploadDocument(userId, dto, file,jwt.getSubject()));
    }

    @PreAuthorize("hasRole('TRAVELER')")
    @GetMapping("/user/{userId}")
    @Operation(summary = "Lista documenti utente", description = "Recupera i documenti di un utente. Accessibile al proprietario, all'organizzatore o all'admin.")
    public ResponseEntity<List<UserDocumentResponseDTO>> getMyDocumentsByUser(
            @PathVariable String userId, @AuthenticationPrincipal Jwt jwt) {
        log.info("Ricevuta richiesta di elenco documenti per l'utente ID: {}", userId);
        return ResponseEntity.ok(userDocumentService.getMyDocuments(userId,jwt.getSubject()));
    }
    @PreAuthorize("hasAnyRole('ORGANIZER','ADMIN')")
    @GetMapping("/admin/document/{userId}")
    @Operation(summary = "Lista documenti utente", description = "Recupera i documenti di un utente. Accessibile al proprietario, all'organizzatore o all'admin.")
    public ResponseEntity<List<UserDocumentResponseDTO>> getDocumentsByUser(
            @PathVariable String userId) {
        log.info("Ricevuta richiesta di elenco documenti per l'utente ID: {}", userId);
        return ResponseEntity.ok(userDocumentService.getUserDocuments(userId));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/verify/{docId}")
    @Operation(summary = "Verifica documento", description = "Approvazione o rifiuto. Solo per Organizzatori o Admin.")
    public ResponseEntity<UserDocumentResponseDTO> verifyDocument(
            @PathVariable UUID docId,
            @RequestParam boolean approved,
            @RequestParam (required = false) String reason){
        log.info("Ricevuta richiesta di verifica per il documento ID: {}. Approvato: {}", docId, approved);
        return ResponseEntity.ok(userDocumentService.verifyDocument(docId, approved, reason));
    }

    @PreAuthorize("hasRole('TRAVELER')")
    @DeleteMapping("/{docId}")
    @Operation(summary = "Elimina documento", description = "Elimina un documento. Solo il proprietario o l'admin possono farlo.")
    public ResponseEntity<Void> deleteDocument(
            @PathVariable UUID docId,
            @AuthenticationPrincipal Jwt jwt){
        log.info("Ricevuta richiesta di eliminazione per il documento ID: {} dal subject JWT: {}", docId, jwt.getSubject());
        userDocumentService.deleteDocument(docId, jwt.getSubject());
        return ResponseEntity.noContent().build();
    }
}