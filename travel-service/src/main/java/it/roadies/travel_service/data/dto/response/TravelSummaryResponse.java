package it.roadies.travel_service.data.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class TravelSummaryResponse {
    private UUID id;
    private String ownerId;
    private String title;
    private String destination;
    private Integer durationDays;
    private BigDecimal startingFromPrice;
    private String type;
    private List<ImageResponse> images;
}
