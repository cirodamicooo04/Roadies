package it.roadies.shared.contracts;

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
