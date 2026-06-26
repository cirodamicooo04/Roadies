package it.roadies.booking_service.data.dto.request;

import jakarta.validation.constraints.AssertTrue;
import lombok.Data;

import java.util.UUID;

@Data
public class BookingDraftRequest {
    private UUID travelId;
    private UUID activityId;

    @AssertTrue(message = "Devi specificare uno e un solo target tra travelId e activityId")
    public boolean isValidTarget() {
        boolean hasTravel = travelId != null;
        boolean hasActivity = activityId != null;

        return (hasTravel && !hasActivity) || (!hasTravel && hasActivity);
    }
}
