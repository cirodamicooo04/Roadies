package it.roadies.travel_service.data.dto.response;

import it.roadies.travel_service.data.entity.enumerations.Visibility;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class FavouriteListResponse {
    private UUID id;
    private String ownerId;
    private String name;
    private Visibility visibility;
    private LocalDateTime createdAt;

    private List<String> sharedWithIds;

    private List<FavouriteListItemResponse> items;
}