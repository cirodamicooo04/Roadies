package it.roadies.travel_service.data.dto.request;

import it.roadies.travel_service.data.entity.enumerations.Status;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ActivityDepartureCreateRequest {
    @Future
    @NotNull
    private LocalDateTime startTimestamp;
    @Future
    @NotNull
    private LocalDateTime endTimestamp;
    @NotNull
    @Min(1)
    private Integer maxSlots;
    @NotNull
    @Positive
    private BigDecimal price;
}
