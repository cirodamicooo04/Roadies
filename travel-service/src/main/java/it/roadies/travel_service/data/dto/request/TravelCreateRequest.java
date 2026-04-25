package it.roadies.travel_service.data.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Singular;

import java.util.List;

@Data
public class TravelCreateRequest {
    @NotBlank @Size(max = 150)
    private String title;
    @NotBlank @Size(max = 10000)
    private String description;
    @NotBlank @Size(max = 150)
    private String destination;
    @NotNull @Min(1)
    private int durationDays;

    @Valid
    private List<TravelDepartureCreateRequest> departures;
    @Valid
    private List<ActivityCreateRequest> activities;
    @Valid
    private List<TravelTagRequest> tagScores;
}




