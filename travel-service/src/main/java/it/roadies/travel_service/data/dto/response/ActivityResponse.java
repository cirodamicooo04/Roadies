package it.roadies.travel_service.data.dto.response;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class ActivityResponse {
    private UUID id;
    private UUID travelId;
    private String ownerId;
    private String name;
    private String description;
    private String destination;
    private String address;
    private Double latitude;
    private Double longitude;
    private Integer dayNumber;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String type;
    private List<ActivityDepartureResponse> departures;
    private List<ImageResponse> images;

}
