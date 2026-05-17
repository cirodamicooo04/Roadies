package it.roadies.review_service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ReplyRequest {
    @NotBlank
    private String content;
}
