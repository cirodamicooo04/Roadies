package it.roadies.travel_service.data.dto.request;

import it.roadies.travel_service.data.entity.Activity;
import it.roadies.travel_service.data.entity.enumerations.Status;
import jakarta.persistence.*;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ActivityDepartureUpdateRequest {
    private UUID id;
    @Future
    private LocalDateTime startTimestamp;
    @Future
    private LocalDateTime endTimestamp;
    @PositiveOrZero
    private BigDecimal price;
}
