package it.roadies.booking_service.data.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class BookingDraftRequest {
    @NotNull
    private String userId;
    private UUID travelId;
    private UUID activityId;
}
