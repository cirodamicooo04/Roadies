package it.roadies.travel_service.data.dto.request;

import it.roadies.travel_service.data.entity.enumerations.Status;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ActivityDepartureCreateRequest {
    private LocalDateTime startTimestamp;
    private LocalDateTime endTimestamp;
    private Integer maxSlots;
    private BigDecimal price;
    private Status status;
}
