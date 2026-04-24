package it.roadies.booking_service.data.dto.response;

import it.roadies.booking_service.data.entities.enumeration.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookingStatusResponse {
    private UUID bookingId;
    private BookingStatus status;
    private UUID travelId;
    private UUID activityId;
}
