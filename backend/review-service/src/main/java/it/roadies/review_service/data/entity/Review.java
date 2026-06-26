package it.roadies.review_service.data.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Data
@Table(name = "reviews",
        uniqueConstraints = @UniqueConstraint(columnNames = {"travel_id", "user_id"}))
@EntityListeners(AuditingEntityListener.class)
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "travel_id", nullable = false)
    private UUID travelId;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(nullable = false)
    private int rating;

    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "review_type", nullable = false)
    private ReviewType reviewType;

    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;

    @CreatedBy
    private String createdBy;

    @LastModifiedBy
    private String lastUpdatedBy;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
