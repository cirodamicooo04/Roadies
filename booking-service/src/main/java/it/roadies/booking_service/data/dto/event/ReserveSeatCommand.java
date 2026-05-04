package it.roadies.booking_service.data.dto.event;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReserveSeatCommand {
    @NotNull
    private UUID bookingId;
    private UUID travelId;
    private UUID activityId;
    @Min(1)
    @Positive
    private Integer peopleCount;

    @AssertTrue(message = "activityId e travelId non posso essere entrambi campi notnull")
    public boolean ValidTarget() {
        boolean hasTravel = travelId != null;
        boolean hasActivity = activityId != null;

        return (hasTravel && !hasActivity) || (!hasTravel && hasActivity);
    }
}
