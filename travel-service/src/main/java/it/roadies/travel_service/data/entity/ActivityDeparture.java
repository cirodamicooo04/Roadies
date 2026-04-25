package it.roadies.travel_service.data.entity;

import it.roadies.travel_service.data.entity.enumerations.Status;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@Table(name = "ACTIVITY_SESSIONS")
public class ActivityDeparture {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "activity_id")
    private Activity activity;

    @Column(name = "start_timestamp", nullable = false)
    private LocalDateTime startTimestamp;

    @Column(name = "end_timestamp", nullable = false)
    private LocalDateTime endTimestamp;

    @Column(name = "max_slots", nullable = false)
    private Integer maxSlots;

    @Column(name = "available_slots", nullable = false)
    private Integer availableSlots;

    @Version
    private Long version;

    @Column(name = "price", nullable = false)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status = Status.PLANNED;

}
