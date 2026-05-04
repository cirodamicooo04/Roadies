package it.roadies.travel_service.data.entity;

import it.roadies.travel_service.data.entity.enumerations.Status;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.SoftDelete;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Data
@Table(name = "TRAVEL_DEPARTURE")
@SoftDelete(columnName = "deleted")
public class TravelDeparture {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "travel_id")
    private Travel travel;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "price", nullable = false)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status = Status.PLANNED;

    @Column(name = "max_slots", nullable = false)
    private Integer maxSlots;

    @Column(name = "available_slots", nullable = false)
    private Integer availableSlots;

    @Version
    private Long version;

    @PrePersist
    public void prePersist() {
        if (this.availableSlots == null) {
            this.availableSlots = maxSlots;
        }
    }
}
