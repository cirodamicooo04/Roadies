package it.roadies.user_service.data.dto.request;
import it.roadies.user_service.data.entities.enumeration.DocumentType;
import lombok.Data;

@Data
public class UserDocumentRequestDTO {
    private DocumentType document_type;
    private String document_number;
    private String file_url;
}
