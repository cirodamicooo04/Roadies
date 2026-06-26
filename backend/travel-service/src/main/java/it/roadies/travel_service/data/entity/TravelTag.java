package it.roadies.travel_service.data.entity;

import it.roadies.travel_service.data.entity.embeddables.TravelTagId;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "TRAVEL_TAG")
@EntityListeners(value = {AuditingEntityListener.class})
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

    @CreatedBy
    private String createdBy;

    @LastModifiedBy
    private String lastUpdatedBy;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Column(name = "score")
    private Integer score;
}
