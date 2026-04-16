package it.roadies.user_service.entities;

import it.roadies.user_service.entities.enumeration.Status;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name= "FRIENDSHIP")
@Data

public class Friendship {

    @Id
    @GeneratedValue()
    private UUID id;

    @ManyToOne
    @JoinColumn(name="requester_id", referencedColumnName = "keycloak_id")
    private User requester_id;

    @ManyToOne
    @JoinColumn(name="receiver_id", referencedColumnName = "keycloak_id")
    private User receiver_id;

    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(nullable = false)
    private LocalDateTime created_at;
}
