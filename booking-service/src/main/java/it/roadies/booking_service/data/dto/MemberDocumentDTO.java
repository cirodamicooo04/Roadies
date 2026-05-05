package it.roadies.booking_service.data.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class MemberDocumentDTO {
    @NotNull
    private UUID id;
    @NotBlank
    private String url;
    private String rejectionReason;
}
