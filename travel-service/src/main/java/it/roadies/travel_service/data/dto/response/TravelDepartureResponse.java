package it.roadies.travel_service.data.dto.response;

import it.roadies.travel_service.data.entity.enumerations.Status;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class TravelDepartureResponse {
    private UUID id;
    private UUID travelId;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal price;
    private Status status;
    private Integer maxSlots;
}
