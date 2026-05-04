package it.roadies.travel_service.data.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class ActivityUpdateRequest {
    @Size(min = 1, max = 100)
    @Pattern(regexp = ".*\\S.*")
    private String name;
    @Pattern(regexp = ".*\\S.*")
    @Size(min = 1, max = 1000)
    private String description;
    @Size(min = 1, max = 200)
    @Pattern(regexp = ".*\\S.*")
    private String destination;
    @Size(min = 1, max = 200)
    @Pattern(regexp = ".*\\S.*")
    private String address;
    @DecimalMin(value = "-90.0") @DecimalMax(value = "90.0")
    private Double latitude;
    @DecimalMin(value = "-180.0") @DecimalMax(value = "180.0")
    private Double longitude;
    @Min(1)
    private Integer dayNumber;
}
