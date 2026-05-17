package it.roadies.review_service.dto;

import java.util.UUID;

import it.roadies.review_service.entity.ReviewType;
import lombok.Data;

@Data
public class ReviewRequest {
    private UUID travelId;
    private int rating;
    private String comment;
    private ReviewType reviewType;
}
