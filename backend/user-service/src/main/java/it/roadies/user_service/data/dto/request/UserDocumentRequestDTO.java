package it.roadies.user_service.data.dto.request;

import it.roadies.user_service.data.entities.enumeration.DocumentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserDocumentRequestDTO {
    @NotNull
    private DocumentType documentType;
    @NotBlank
    @Size(min = 5, max = 50)
    private String documentNumber;
}