package it.roadies.booking_service.data.dto.response;

import jakarta.validation.constraints.AssertTrue;
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

    @AssertTrue(message = "activityId e travelId non posso essere entrambi campi notnull")
    public boolean ValidTarget() {
        boolean hasTravel = travelId != null;
        boolean hasActivity = activityId != null;

        return (hasTravel && !hasActivity) || (!hasTravel && hasActivity);
    }
}
