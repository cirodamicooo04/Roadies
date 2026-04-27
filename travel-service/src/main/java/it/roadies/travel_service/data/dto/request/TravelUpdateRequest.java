package it.roadies.travel_service.data.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

@Data
public class TravelUpdateRequest {
    @Size(min = 1, max = 150)
    @Pattern(regexp = ".*\\S.*")
    private String title;
    @Size(min = 1, max = 10000)
    @Pattern(regexp = ".*\\S.*")
    private String description;
    @Size(min = 1, max = 150)
    @Pattern(regexp = ".*\\S.*")
    private String destination;
    @DecimalMin(value = "-90.0") @DecimalMax(value = "90.0")
    private Double latitude;
    @DecimalMin(value = "-180.0") @DecimalMax(value = "180.0")
    private Double longitude;
    @Min(1)
    private Integer durationDays;

    @Valid
    private List<TravelTagRequest> tagScores;
}
