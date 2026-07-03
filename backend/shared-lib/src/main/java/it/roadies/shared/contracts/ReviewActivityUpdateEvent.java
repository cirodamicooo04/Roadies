package it.roadies.shared.contracts;

import lombok.Data;

import java.util.UUID;

@Data
public class ReviewActivityUpdateEvent {
    private UUID activityId;
    private Double averageRating;
    private Integer numberOfRatings;
}
