package it.roadies.review_service.data.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Data;

@Data
public class ReplyResponse {
    private UUID id;
    private UUID reviewId;
    private String content;
    private String userId;
    private LocalDateTime createdAt;
}
