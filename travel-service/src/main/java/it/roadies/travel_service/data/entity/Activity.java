package it.roadies.travel_service.data.entity;

import it.roadies.travel_service.data.entity.enumerations.Continent;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SoftDelete;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Data
@Table(name = "ACTIVITY")
@SoftDelete(columnName = "deleted")
@EntityListeners(value = {AuditingEntityListener.class})
public class Activity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "travel_id")
    private Travel travel;

    @Column(name = "owner_id", nullable = false)
    private String ownerId;

    @Column(length = 100, name = "name", nullable = false)
    private String name;

    @Column(length = 1000, name = "description", nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "continent", nullable = false)
    private Continent continent;

    @Column(name = "country", nullable = false)
    private String country;

    @Column(length = 200, name = "destination", nullable = false)
    private String destination;

    @Column(length = 500, name = "address")
    private String address;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "day_number")
    private Integer dayNumber;

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

    @Column(name = "average_rating")
    private Double averageRating = 0.0;

    @Column(name = "number_of_ratings")
    private Integer numberOfRatings = 0;

    @OneToMany(mappedBy = "activity", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ActivityDeparture> departures;

    @OneToMany(mappedBy = "activity", cascade = CascadeType.ALL)
    private List<Image> images = new ArrayList<>();


}