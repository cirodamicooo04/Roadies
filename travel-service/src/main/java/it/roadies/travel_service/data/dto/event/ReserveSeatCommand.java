package it.roadies.travel_service.data.dto.event;

import jakarta.validation.constraints.Min;
import lombok.Data;

import java.util.UUID;

@Data
public class ReserveSeatCommand {
    private UUID bookingId;
    private UUID travelId;
    private UUID activityId;
    @Min(1)
    private Integer peopleCount;
}
