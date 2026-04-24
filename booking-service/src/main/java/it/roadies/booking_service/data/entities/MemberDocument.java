package it.roadies.booking_service.data.entities;

import it.roadies.booking_service.data.entities.enumeration.DocumentStatus;
import it.roadies.booking_service.data.entities.enumeration.DocumentType;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "MEMBER_DOCUMENT")
@Data
public class MemberDocument {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DocumentType type;

    @Column(name = "file_url", nullable = false)
    private String fileUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DocumentStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "rejection_reason")
    private String rejectionReason;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private BookingMember member;

    @PrePersist
    public void setStatusAndCreateAt() {
        if (this.status == null) {
            this.status = DocumentStatus.PENDING;
        }
        this.createdAt = LocalDateTime.now();
    }
}
