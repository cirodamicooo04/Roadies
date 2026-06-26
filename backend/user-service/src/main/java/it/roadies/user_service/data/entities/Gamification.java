package it.roadies.user_service.data.entities;

import it.roadies.user_service.data.entities.enumeration.Badge;
import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name= "GAMIFICATION")
@Data
@EntityListeners(AuditingEntityListener.class)
public class Gamification {
    @Id
    private String userId;

    @OneToOne
    @MapsId
    @JoinColumn(name="userId", referencedColumnName = "keycloakId")
    private User user;

    @Column
    private Long points;

    @Enumerated(EnumType.STRING)
    private Badge badge= Badge.BRONZE;
}
