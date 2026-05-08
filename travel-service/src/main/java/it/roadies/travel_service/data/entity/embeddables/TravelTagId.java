package it.roadies.travel_service.data.entity.embeddables;

import jakarta.persistence.Embeddable;
import lombok.Data;

import java.io.Serializable;
import java.util.UUID;


@Embeddable
@Data
public class TravelTagId implements Serializable {
    private UUID travelId;
    private UUID tagId;
}
