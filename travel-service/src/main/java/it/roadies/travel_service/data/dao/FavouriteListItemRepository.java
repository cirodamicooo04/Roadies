package it.roadies.travel_service.data.dao;

import it.roadies.travel_service.data.entity.FavouriteListItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface FavouriteListItemRepository extends JpaRepository<FavouriteListItem, UUID> {
    void deleteByListIdAndTravelId(UUID listId, UUID travelId);
}
