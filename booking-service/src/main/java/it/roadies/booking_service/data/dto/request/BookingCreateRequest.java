package it.roadies.booking_service.data.dto.request;

import jakarta.validation.constraints.Min;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class BookingCreateRequest {
    private String userId;
    private UUID travelId;
    private UUID activityId;
    private UUID bookingId;
    @Min(1)
    private Integer peopleCount;
    @Min(1)
    private BigDecimal totalPrice;
}
