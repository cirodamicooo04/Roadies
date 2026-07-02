package it.roadies.travel_service.data.dto.request;

import it.roadies.travel_service.data.entity.Travel;
import it.roadies.travel_service.data.entity.enumerations.Status;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class TravelDepartureUpdateRequest {
    private UUID id;

    @Future(message = "The start date can't be in the past")
    private LocalDate startDate;

    @Future
    private LocalDate endDate;

    @Positive
    private BigDecimal price;

    @Positive
    private Integer maxSlots;
}
