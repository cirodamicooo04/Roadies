package it.roadies.booking_service.data.dto.request;

import it.roadies.booking_service.data.entities.enumeration.DocumentType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MemberDocumentRequest {
    @NotNull
    private DocumentType type;
}
