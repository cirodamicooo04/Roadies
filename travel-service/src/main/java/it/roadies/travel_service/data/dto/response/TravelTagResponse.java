package it.roadies.travel_service.data.dto.response;

import it.roadies.travel_service.data.entity.Tag;
import it.roadies.travel_service.data.entity.Travel;
import it.roadies.travel_service.data.entity.embeddables.TravelTagId;
import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Data
public class TravelTagResponse {
    private UUID tagId;
    private String tagName;
    private Integer score;
}
