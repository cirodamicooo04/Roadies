package it.roadies.review_service.data.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ReplyRequest {

    @NotBlank(message = "The content field cannot be blank.")
    private String content;

}
