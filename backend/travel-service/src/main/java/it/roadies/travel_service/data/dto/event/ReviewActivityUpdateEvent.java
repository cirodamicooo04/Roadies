package it.roadies.travel_service.data.dto.event;

import lombok.Data;

import java.util.UUID;

@Data
public class ReviewActivityUpdateEvent {
    private UUID activityId;
    private Double averageRating;
    private Integer numberOfRatings;
}
