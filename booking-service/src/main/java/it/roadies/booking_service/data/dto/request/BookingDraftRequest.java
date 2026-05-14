package it.roadies.booking_service.data.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class BookingDraftRequest {
    private UUID travelId;
    private UUID activityId;

    @AssertTrue(message = "activityId e travelId non posso essere entrambi campi notnull")
    public boolean ValidTarget() {
        boolean hasTravel = travelId != null;
        boolean hasActivity = activityId != null;

        return (hasTravel && !hasActivity) || (!hasTravel && hasActivity);
    }
}
