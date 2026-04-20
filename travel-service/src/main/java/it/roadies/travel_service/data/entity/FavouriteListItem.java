package it.roadies.travel_service.data.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@Table(name = "FAVOURITE_LIST_ITEM")
public class FavouriteListItem {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "list_id", nullable = false)
    private FavouriteList list;

    @ManyToOne
    @JoinColumn(name = "travel_id")
    private Travel travel;

    @ManyToOne
    @JoinColumn(name = "activity_id")
    private Activity activity;

    @CreationTimestamp
    @Column(name = "added_at")
    private LocalDateTime addedAt;

}
