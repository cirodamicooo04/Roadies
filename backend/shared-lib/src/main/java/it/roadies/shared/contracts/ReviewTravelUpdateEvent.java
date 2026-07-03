package it.roadies.shared.contracts;

import lombok.Data;

import java.util.UUID;

@Data
public class ReviewTravelUpdateEvent {
    private UUID travelId;
    private Double averageRating;
    private Integer numberOfRatings;
}
