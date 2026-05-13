package it.roadies.user_service.data.entities;

import it.roadies.user_service.data.entities.enumeration.DocumentStatus;
import it.roadies.user_service.data.entities.enumeration.DocumentType;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "USER_DOCUMENTS")
@Data
public class UserDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "documentType", nullable = false)
    private DocumentType documentType;

    @Column(name = "documentNumber", nullable = false, unique = true)
    private String documentNumber;

    @Column(name = "fileUrl", nullable = false)
    private String fileUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DocumentStatus status;

    @Column(name = "createdAt", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "rejectionReason")
    private String rejectionReason;

    @Column(name = "verifiedAt")
    private LocalDateTime verifiedAt;

    @ManyToOne
    @JoinColumn(name = "userId", referencedColumnName = "keycloakId", nullable = false)
    private User userId;

    @PrePersist
    public void onPrePersist() {
        if (this.status == null) {
            this.status = DocumentStatus.PENDING;
        }
        this.createdAt = LocalDateTime.now();
    }
}