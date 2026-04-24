package it.roadies.user_service.data.dto.response;

import it.roadies.user_service.data.entities.enumeration.DocumentStatus;
import it.roadies.user_service.data.entities.enumeration.DocumentType;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class UserDocumentResponseDTO {
    private UUID id;
    private DocumentType document_type;
    private String document_number;
    private String file_url;
    private DocumentStatus status;
    private String rejection_reason;
    private LocalDateTime created_at;
    private String userId;
}
