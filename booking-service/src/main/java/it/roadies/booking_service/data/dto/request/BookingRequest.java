package it.roadies.booking_service.data.dto.request;

import lombok.Data;

import java.util.UUID;

@Data
public class BookingRequest {
    private String userId;
    private UUID travelId;
    private Integer peopleCount;
}
