package it.roadies.travel_service.data.dto.response;

import it.roadies.travel_service.data.entity.enumerations.Status;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ActivityDeparturesResponse {
    private UUID id;
    private UUID activityId;
    private LocalDateTime startTimestamp;
    private LocalDateTime endTimestamp;
    private Integer maxSlots;
    private BigDecimal price;
    private Status status;
}
