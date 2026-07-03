package it.roadies.chat_service.data.entities;

import it.roadies.chat_service.data.entities.Message;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "conversations",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_conversation_traveler_organizer_travel",
                columnNames = {"traveler_id", "organizer_id", "travel_id"}
        ))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "traveler_id", nullable = false)
    private String travelerId;

    @Column(name = "organizer_id", nullable = false)
    private String organizerId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Message> messages = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
