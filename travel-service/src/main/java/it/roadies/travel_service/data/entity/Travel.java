package it.roadies.travel_service.data.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Entity
@Data
@Table(name = "TRAVELS")
public class Travel {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;

    @Column(length = 150, name = "title", nullable = false)
    private String title;

    @Column(length = 10000, name = "description", nullable = false)
    private String description;

    @Column(length = 150, name = "destination", nullable = false)
    private String destination;

    @Column(name = "duration_days", nullable = false)
    private int durationDays;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "travel")
    private Set<TravelDeparture> departures;

    @OneToMany(mappedBy = "travel")
    private Set<Activity> activities;

    @OneToMany(mappedBy = "travel", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TravelTag> tagScores;

}


