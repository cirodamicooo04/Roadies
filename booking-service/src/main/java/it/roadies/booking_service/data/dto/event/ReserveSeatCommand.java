package it.roadies.booking_service.data.dto.event;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReserveSeatCommand {
    private UUID bookingId;
    private UUID travelId;
    private UUID activityId;
    @Min(1)
    private Integer peopleCount;
}
