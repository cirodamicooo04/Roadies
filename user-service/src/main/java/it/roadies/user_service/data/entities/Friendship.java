package it.roadies.user_service.data.entities;

import it.roadies.user_service.data.entities.enumeration.Status;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name= "FRIENDSHIP")
@Data

public class Friendship {

    @Id
    @GeneratedValue()
    private UUID id;

    @ManyToOne
    @JoinColumn(name="requesterId", referencedColumnName = "keycloakId")
    private User requesterId;

    @ManyToOne
    @JoinColumn(name="receiverId", referencedColumnName = "keycloakId")
    private User receiverId;

    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}
