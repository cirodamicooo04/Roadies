package it.roadies.booking_service.data.dto.response;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservationResponse {
    private UUID travelId;
    private UUID activityId;
    @Min(1)
    private Integer seatsReserved;
    private BigDecimal pricePerPerson;
}
