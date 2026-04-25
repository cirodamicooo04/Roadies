package it.roadies.booking_service.data.dto.event;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
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
    private Integer peopleCount;
}
