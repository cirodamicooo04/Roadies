package it.roadies.travel_service.data.dto.response;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class FavouriteListItemResponse {
    private UUID id;
    private LocalDateTime addedAt;
    private TravelSummaryResponse travel;
    private ActivitySummaryResponse activity;
}