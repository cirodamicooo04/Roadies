package it.roadies.travel_service.data.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Entity
@Data
@Table(name = "FAVOURITE_LIST_SHARED")
public class FavouriteListShared {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "list_id", nullable = false)
    private FavouriteList list;

    @Column(name = "user_id", nullable = false)
    private UUID userId;
}