package it.roadies.travel_service.data.dto.response;

import it.roadies.travel_service.data.entity.TravelTag;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class TravelResponse {
    private UUID id;
    private String ownerId;
    private String title;
    private String description;
    private String destination;
    private int durationDays;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private List<TravelDepartureResponse> departures;
    private List<ActivityResponse> activities;
    private List<TravelTagResponse> tagScores;
}
