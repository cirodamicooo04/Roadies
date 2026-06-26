package it.roadies.review_service.data.dto;

import it.roadies.review_service.data.entity.ReviewType;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Data;

@Data
public class ReviewResponse {
    private UUID id;
    private UUID travelId;
    private ReviewType reviewType;
    private String userId;
    private int rating;
    private String content;
    private LocalDateTime createdAt;
}