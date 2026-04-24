package it.roadies.user_service.controller;

import io.swagger.v3.oas.annotations.parameters.RequestBody;
import it.roadies.user_service.data.dto.request.UserDocumentRequestDTO;
import it.roadies.user_service.data.dto.response.UserDocumentResponseDTO;
import it.roadies.user_service.services.UserDocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/documents")
@RequiredArgsConstructor
public class UserDocumentController {
    private final UserDocumentService userDocumentService;

    @PostMapping("/upload/{userId}")
    public ResponseEntity<UserDocumentResponseDTO> upload(
            @PathVariable String userId,
            @RequestBody UserDocumentRequestDTO dto){
        return ResponseEntity.ok(userDocumentService.uploadDocument(userId,dto));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<UserDocumentResponseDTO>> getDocumentsByUser(
            @PathVariable String userId) {
        return ResponseEntity.ok(userDocumentService.getUserDocuments(userId));
    }

    @PreAuthorize("hasRole('ORGANIZER')")
    @PatchMapping("/verify/{docId}")
    public ResponseEntity<UserDocumentResponseDTO> verifyDocument(
            @PathVariable UUID docId,
            @RequestParam boolean approved,
            @RequestParam (required = false) String reason){
        return ResponseEntity.ok(userDocumentService.verifyDocument(docId,approved,reason));
    }

    @DeleteMapping("/{docId}")
    public ResponseEntity<Void> deleteDocument(
            @PathVariable UUID docId){
        userDocumentService.deleteDocument(docId);
        return ResponseEntity.noContent().build();
    }
}
