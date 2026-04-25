package it.roadies.travel_service.data.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class TravelTagRequest {
    @NotNull
    private UUID tagId;
    @Min(1)
    @Max(5)
    private int score;
}
