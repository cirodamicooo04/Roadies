package it.roadies.travel_service.data.entity;

import it.roadies.travel_service.data.entity.enumerations.Continent;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Formula;
import org.hibernate.annotations.SoftDelete;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "TRAVELS")
@SoftDelete(columnName = "deleted")
@EntityListeners(value = {AuditingEntityListener.class})
public class Travel {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "owner_id", nullable = false)
    private String ownerId;

    @Column(length = 150, name = "title", nullable = false)
    private String title;

    @Column(length = 10000, name = "description", nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "continent", nullable = false)
    private Continent continent;

    @Column(name = "country", nullable = false)
    private String country;

    @Column(length = 150, name = "destination", nullable = false)
    private String destination;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "duration_days", nullable = false)
    private int durationDays;

    @CreatedBy
    private String createdBy;

    @LastModifiedBy
    private String lastUpdatedBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "average_rating", nullable = false)
    private Double averageRating = 0.0;

    @Column(name = "number_of_ratings", nullable = false)
    private Integer numberOfRatings = 0;

    @Formula("(SELECT MIN(td.price) FROM TRAVEL_DEPARTURE td WHERE td.travel_id = id)")
    private BigDecimal startingFromPrice = BigDecimal.ZERO;

    @OneToMany(mappedBy = "travel", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TravelDeparture> departures = new ArrayList<>();

    @OneToMany(mappedBy = "travel", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Activity> activities = new ArrayList<>();

    @OneToMany(mappedBy = "travel", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TravelTag> tagScores = new ArrayList<>();

    @OneToMany(mappedBy = "travel", cascade = CascadeType.ALL)
    private List<Image> images = new ArrayList<>();

}


