package it.roadies.travel_service.data.dto.request;

import it.roadies.travel_service.data.entity.enumerations.Continent;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class ActivityCreateRequest {
    @NotBlank @Size(max = 100)
    private String name;
    @NotBlank @Size(max = 1000)
    private String description;
    @NotBlank @Size(max = 200)
    private String destination;
    @NotNull
    private Continent continent;
    @Pattern(regexp = ".*\\S.*")
    private String country;
    @NotBlank @Size(max = 200)
    private String address;
    @DecimalMin(value = "-90.0") @DecimalMax(value = "90.0")
    private Double latitude;
    @DecimalMin(value = "-180.0") @DecimalMax(value = "180.0")
    private Double longitude;
    @Min(1)
    private Integer dayNumber;

    private List<UUID> imageIds;

    @Valid
    private List<ActivityDepartureCreateRequest> departures;

}
