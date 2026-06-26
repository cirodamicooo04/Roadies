package it.roadies.travel_service.data.dto.response;

import it.roadies.travel_service.data.entity.enumerations.Continent;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
public class ActivitySummaryResponse {
    private UUID id;
    private String ownerId;
    private String name;
    private Continent continent;
    private String country;
    private String destination;
    private BigDecimal startingFromPrice;
    private String type;
    private Double averageRating;
    private List<ImageResponse> images;
}
