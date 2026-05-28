package it.roadies.review_service.data.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class ReplyUpdateRequest {

    @NotNull
    private UUID replyId;

    @NotNull
    private String content;

}
