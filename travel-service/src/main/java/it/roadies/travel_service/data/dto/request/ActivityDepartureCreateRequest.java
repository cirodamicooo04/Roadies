package it.roadies.travel_service.data.dto.request;

import it.roadies.travel_service.data.entity.enumerations.Status;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ActivityDepartureCreateRequest {
    @Future
    private LocalDateTime startTimestamp;
    @Future
    private LocalDateTime endTimestamp;
    @NotNull
    @Min(1)
    private Integer maxSlots;
    @NotNull
    @PositiveOrZero
    private BigDecimal price;
}
