package it.roadies.review_service.data.event;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class ReviewTravelUpdateEvent {
    private UUID travelId;
    private Double averageRating;
    private Integer numberOfRatings;

}
