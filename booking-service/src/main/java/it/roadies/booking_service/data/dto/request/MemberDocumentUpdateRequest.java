package it.roadies.booking_service.data.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.util.UUID;

@Data
public class MemberDocumentUpdateRequest {
    @NotNull
    private UUID id;
    private String url;
    @Size(max = 1000)
    private String rejectionReason;
}