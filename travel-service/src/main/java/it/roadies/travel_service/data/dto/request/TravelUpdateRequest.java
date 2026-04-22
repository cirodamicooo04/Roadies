package it.roadies.travel_service.data.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class TravelUpdateRequest {
    @Size(min = 1, max = 150)
    private String title;
    @Size(min = 1, max = 10000)
    private String description;
    @Size(min = 1, max = 150)
    private String destination;
    @Min(1)
    private Integer durationDays;

    @Valid
    private List<TravelDepartureUpdateRequest> departures;
    @Valid
    private List<ActivityUpdateRequest> activities;
    @Valid
    private List<TravelTagRequest> tagScores;
}
