package it.roadies.travel_service.data.entity;

import it.roadies.travel_service.data.entity.embeddables.TravelTagId;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "TRAVEL_TAG")
public class TravelTag {
    @EmbeddedId
    private TravelTagId travelTagId = new TravelTagId();

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
