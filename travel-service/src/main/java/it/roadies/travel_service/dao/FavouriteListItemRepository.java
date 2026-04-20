package it.roadies.travel_service.dao;

import it.roadies.travel_service.entity.FavouriteListItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface FavouriteListItemRepository extends JpaRepository<FavouriteListItem, UUID> {
}
