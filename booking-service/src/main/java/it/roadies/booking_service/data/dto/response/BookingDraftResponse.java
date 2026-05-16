package it.roadies.booking_service.data.dto.response;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class BookingDraftResponse {
    @NotNull
    private UUID bookingId;
}
