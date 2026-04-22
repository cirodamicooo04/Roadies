package it.roadies.booking_service.data.dto.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookingCreatedEvent {
    private UUID bookingId;
    private UUID travelId;
    private int spots;
}
