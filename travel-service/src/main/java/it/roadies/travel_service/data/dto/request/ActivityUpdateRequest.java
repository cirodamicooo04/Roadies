package it.roadies.travel_service.data.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class ActivityUpdateRequest {
    private UUID id;
    @Size(min = 1, max = 100)
    private String name;
    @Size(min = 1, max = 1000)
    private String description;
    @Size(min = 1, max = 200)
    private String location;
    @Min(1)
    private Integer dayNumber;
    @Valid
    private List<ActivityDepartureUpdateRequest> departures;
}
