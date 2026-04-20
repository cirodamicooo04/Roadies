package it.roadies.travel_service.entity;

import it.roadies.travel_service.entity.embeddables.TravelTagId;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "TRAVEL_TAG")
public class TravelTag {
    @EmbeddedId
    private TravelTagId travelTagId;

    @ManyToOne
    @MapsId("travelId")
    @JoinColumn(name = "travel_id")
    private Travel travel;

    @ManyToOne
    @MapsId("tagId")
    @JoinColumn(name = "tag_id")
    private Tag tag;

    @Column(name = "score")
    private Integer score;
}
