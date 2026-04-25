package it.roadies.travel_service.data.dto.client;

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
    private Integer seatsReserved;
    private BigDecimal pricePerPerson;
}