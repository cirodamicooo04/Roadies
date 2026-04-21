package it.roadies.travel_service.data.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class TravelCreateRequest {
    private String title;
    private String description;
    private String destination;
    private int durationDays;
    private List<TravelDepartureCreateRequest> departures;
    private List<ActivityCreateRequest> activities;
    private List<TravelTagRequest> tagScores;
}




