package it.roadies.travel_service.data.dto.event;

import lombok.Data;

import java.util.UUID;

@Data
public class ReviewTravelUpdateEvent {
    private UUID travelId;
    private Double averageRating;
    private Integer numberOfRatings;
}
