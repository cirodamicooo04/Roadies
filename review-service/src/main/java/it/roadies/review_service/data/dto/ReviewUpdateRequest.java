package it.roadies.review_service.data.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class ReviewUpdateRequest {

    @Min(value = 1, message = "The rating must be at least 1")
    @Max(value = 5, message = "The rating must be at most 5")
    private int rating;

    @NotBlank(message = "The comment field cannot be blank.")
    private String content;
}
