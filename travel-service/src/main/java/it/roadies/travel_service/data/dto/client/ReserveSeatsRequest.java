package it.roadies.travel_service.data.dto.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReserveSeatsRequest {
    private UUID travelId;
    private UUID activityId;
    private Integer peopleCount;
}
