package it.roadies.user_service.data.dto.request;

import it.roadies.user_service.data.entities.enumeration.DocumentType;
import lombok.Data;

@Data
public class UserDocumentRequestDTO {
    private DocumentType documentType;
    private String documentNumber;
}