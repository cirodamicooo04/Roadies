package it.roadies.user_service.entities;

import it.roadies.user_service.entities.enumeration.Badge;
import it.roadies.user_service.entities.enumeration.Status;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name= "GAMIFICATION")
@Data


public class Gamification {
    @Id
    private String user_id;

    @OneToOne
    @MapsId
    @JoinColumn(name="user_id")
    private User user;

    @Column
    private Long points;

    @Enumerated(EnumType.STRING)
    private Badge badge= Badge.BRONZE;
}
