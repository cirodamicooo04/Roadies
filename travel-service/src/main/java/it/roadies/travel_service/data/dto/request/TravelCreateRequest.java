package it.roadies.travel_service.data.dto.request;

import it.roadies.travel_service.data.entity.enumerations.Continent;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.Singular;

import java.util.List;
import java.util.UUID;

@Data
public class TravelCreateRequest {
    @NotBlank @Size(max = 150)
    private String title;
    @NotBlank @Size(max = 10000)
    private String description;
    @NotNull
    private Continent continent;
    @Pattern(regexp = ".*\\S.*")
    private String country;
    @Pattern(regexp = ".*\\S.*")
    private String destination;
    @DecimalMin(value = "-90.0") @DecimalMax(value = "90.0")
    private Double latitude;
    @DecimalMin(value = "-180.0") @DecimalMax(value = "180.0")
    private Double longitude;
    @NotNull @Min(1)
    private int durationDays;

    private List<UUID> imageIds;

    @Valid
    private List<TravelDepartureCreateRequest> departures;
    @Valid
    private List<ActivityCreateRequest> activities;
    @Valid
    private List<TravelTagRequest> tagScores;
}




