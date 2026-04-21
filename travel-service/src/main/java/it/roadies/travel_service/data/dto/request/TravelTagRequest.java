package it.roadies.travel_service.data.dto.request;

import lombok.Data;

import java.util.UUID;

@Data
public class TravelTagRequest {
    private UUID tagId;
    private int score;
}
