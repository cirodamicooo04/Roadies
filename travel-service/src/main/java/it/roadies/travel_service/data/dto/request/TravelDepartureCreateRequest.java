package it.roadies.travel_service.data.dto.request;

import it.roadies.travel_service.data.entity.enumerations.Status;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class TravelDepartureCreateRequest {
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal price;
    private Status status;
    private Integer maxSlots;
}
