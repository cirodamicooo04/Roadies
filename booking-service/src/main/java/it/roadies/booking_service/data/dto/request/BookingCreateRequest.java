package it.roadies.booking_service.data.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class BookingCreateRequest {
    @NotNull
    private String userId;
    private UUID travelId;
    private UUID activityId;
    @NotNull
    private UUID bookingId;
    @Min(1)
    private Integer peopleCount;
    @Min(1)
    private BigDecimal totalPrice;

    @AssertTrue(message = "activityId e travelId non posso essere entrambi campi notnull")
    public boolean ValidTarget() {
        boolean hasTravel = travelId != null;
        boolean hasActivity = activityId != null;

        return (hasTravel && !hasActivity) || (!hasTravel && hasActivity);
    }
}
