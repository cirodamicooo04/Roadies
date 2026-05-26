package it.roadies.travel_service.data.dto.request;

import it.roadies.travel_service.data.entity.Activity;
import it.roadies.travel_service.data.entity.enumerations.Status;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
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
    @Positive
    private BigDecimal price;
}
