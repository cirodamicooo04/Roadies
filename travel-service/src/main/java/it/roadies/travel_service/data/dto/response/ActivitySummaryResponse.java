package it.roadies.travel_service.data.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
public class ActivitySummaryResponse {
    private UUID id;
    private String ownerId;
    private String name;
    private String destination;
    private BigDecimal startingFromPrice;
    private String type;
    private List<ImageResponse> images;
}
