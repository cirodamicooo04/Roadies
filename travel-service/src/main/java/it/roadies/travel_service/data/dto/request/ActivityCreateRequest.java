package it.roadies.travel_service.data.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class ActivityCreateRequest {
    private String name;
    private String description;
    private String location;
    private Integer dayNumber;
    private List<ActivityDepartureCreateRequest> departures;

}
