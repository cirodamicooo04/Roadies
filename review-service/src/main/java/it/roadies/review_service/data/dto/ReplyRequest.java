package it.roadies.review_service.data.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class ReplyRequest {

    @NotBlank(message = "The content field cannot be blank.")
    private String content;

}
