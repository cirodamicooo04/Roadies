package it.roadies.travel_service.data.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class OrganizerTravelsActivityResponse {
    List<TravelSummaryResponse> travels;
    List<ActivitySummaryResponse> activities;
}
