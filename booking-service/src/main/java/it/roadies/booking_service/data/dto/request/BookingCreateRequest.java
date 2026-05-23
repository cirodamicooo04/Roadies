package it.roadies.booking_service.data.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class BookingCreateRequest {
    private UUID travelId;
    private UUID activityId;
    @NotNull
    private UUID bookingId;
    @Min(1)
    private Integer peopleCount;

    @AssertTrue(message = "Devi specificare uno e un solo target tra travelId e activityId")
    public boolean isValidTarget() {
        boolean hasTravel = travelId != null;
        boolean hasActivity = activityId != null;

        return (hasTravel && !hasActivity) || (!hasTravel && hasActivity);
    }
}
