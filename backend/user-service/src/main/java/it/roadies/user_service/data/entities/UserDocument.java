package it.roadies.user_service.data.entities;

import it.roadies.user_service.data.entities.enumeration.DocumentStatus;
import it.roadies.user_service.data.entities.enumeration.DocumentType;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "USER_DOCUMENTS")
@Data
@EntityListeners(AuditingEntityListener.class)
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

    // Se si vuole implementare lo status dei documenti scommentare:
    // @Enumerated(EnumType.STRING)
    // @Column(nullable = false)
    // private DocumentStatus status = DocumentStatus.PENDING;

    @CreationTimestamp
    @Column(name = "createdAt", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updatedAt", nullable = false)
    private LocalDateTime updatedAt;

    @CreatedBy
    @Column(name = "createdBy", nullable = false, updatable = false)
    private String createdBy;

    @LastModifiedBy
    @Column(name = "updatedBy", nullable = false)
    private String updateBy;

    // Se si vuole implementare lo status dei documenti scommentare:
    // @Column(name = "rejectionReason")
    // private String rejectionReason;

    // @Column(name = "verifiedAt")
    // private LocalDateTime verifiedAt;

    @ManyToOne
    @JoinColumn(name = "userId", referencedColumnName = "keycloakId", nullable = false)
    private User userId;
}