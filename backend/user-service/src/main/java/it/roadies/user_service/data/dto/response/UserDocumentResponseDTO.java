package it.roadies.user_service.data.dto.response;

import it.roadies.user_service.data.entities.enumeration.DocumentStatus;
import it.roadies.user_service.data.entities.enumeration.DocumentType;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class UserDocumentResponseDTO {
    private UUID id;
    private DocumentType documentType;
    private String documentNumber;
    private String fileUrl;
    // Se si vuole implementare lo status dei documenti scommentare:
    // private DocumentStatus status;
    // private String rejectionReason;
    private LocalDateTime createdAt;
    private String userId;
}
