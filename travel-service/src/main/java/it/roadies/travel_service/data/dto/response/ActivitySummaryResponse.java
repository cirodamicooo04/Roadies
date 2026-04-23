package it.roadies.travel_service.data.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class ActivitySummaryResponse {
    private UUID id;
    private String ownerId;
    private String name;
    private String location;
    private BigDecimal startingFromPrice;
}
