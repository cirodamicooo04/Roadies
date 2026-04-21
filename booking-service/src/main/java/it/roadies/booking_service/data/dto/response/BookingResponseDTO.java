package it.roadies.booking_service.data.dto.response;

import lombok.Data;

import java.util.UUID;

@Data
public class BookingResponseDTO {
    private UUID id;
    private UUID travelId;
    private Integer peopleCount;
}
