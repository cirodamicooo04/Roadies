package it.roadies.travel_service.data.dto.request;

import it.roadies.travel_service.data.entity.enumerations.Status;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class TravelDepartureCreateRequest {
    @NotNull
    @Future
    private LocalDate startDate;
    @NotNull
    @Future
    private LocalDate endDate;
    @NotNull
    @Positive
    private BigDecimal price;
    @NotNull
    @Positive
    private Integer maxSlots;
}
