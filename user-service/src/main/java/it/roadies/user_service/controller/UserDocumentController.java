package it.roadies.user_service.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.roadies.user_service.data.dto.request.UserDocumentRequestDTO;
import it.roadies.user_service.data.dto.response.UserDocumentResponseDTO;
import it.roadies.user_service.services.UserDocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/documents")
@RequiredArgsConstructor
@Tag(name = "Document Management", description = "API per l'upload e la verifica dei documenti degli utenti")
public class UserDocumentController {
    private final UserDocumentService userDocumentService;

    @PostMapping(value = "/upload/{userId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Carica documento", description = "Carica un documento. Solo l'utente stesso può farlo.")
    public ResponseEntity<UserDocumentResponseDTO> upload(
            @PathVariable String userId,
            @RequestPart("document") UserDocumentRequestDTO dto,
            @RequestPart("file") MultipartFile file){
        return ResponseEntity.ok(userDocumentService.uploadDocument(userId, dto, file));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Lista documenti utente", description = "Recupera i documenti di un utente. Accessibile al proprietario, all'organizzatore o all'admin.")
    public ResponseEntity<List<UserDocumentResponseDTO>> getDocumentsByUser(
            @PathVariable String userId) {
        return ResponseEntity.ok(userDocumentService.getUserDocuments(userId));
    }

    @PatchMapping("/verify/{docId}")
    @Operation(summary = "Verifica documento", description = "Approvazione o rifiuto. Solo per Organizzatori o Admin.")
    public ResponseEntity<UserDocumentResponseDTO> verifyDocument(
            @PathVariable UUID docId,
            @RequestParam boolean approved,
            @RequestParam (required = false) String reason){
        return ResponseEntity.ok(userDocumentService.verifyDocument(docId, approved, reason));
    }

    @DeleteMapping("/{docId}")
    @Operation(summary = "Elimina documento", description = "Elimina un documento. Solo il proprietario o l'admin possono farlo.")
    public ResponseEntity<Void> deleteDocument(
            @PathVariable UUID docId,
            @AuthenticationPrincipal Jwt jwt){
        userDocumentService.deleteDocument(docId, jwt.getSubject());
        return ResponseEntity.noContent().build();
    }
}