package it.roadies.shared.contracts;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class ReviewActivityUpdateEvent {
    private UUID activityId;
    private Double averageRating;
    private Integer numberOfRatings;
}
