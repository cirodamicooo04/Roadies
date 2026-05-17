package it.roadies.review_service.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Data;

@Entity
@Data
@Table(name = "reviews",
        uniqueConstraints = @UniqueConstraint(columnNames = {"travel_id", "user_id"}))
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "travel_id", nullable = false)
    private UUID travelId;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Min(1)
    @Max(5)
    @Column(nullable = false)
    private int rating;

    private String comment;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
