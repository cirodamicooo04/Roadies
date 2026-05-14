package it.roadies.booking_service.data.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.UUID;

@Data
public class MemberDocumentUpdateRequest {
    @NotNull
    private UUID id;
    private String url;
    private String rejectionReason;
}