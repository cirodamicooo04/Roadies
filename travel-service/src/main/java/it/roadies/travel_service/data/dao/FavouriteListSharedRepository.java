package it.roadies.travel_service.data.dao;

import it.roadies.travel_service.data.entity.FavouriteListShared;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface FavouriteListSharedRepository extends JpaRepository<FavouriteListShared, UUID> {
    boolean existsByListIdAndUserId(UUID listId, String userId);

    void deleteByListIdAndUserId(UUID listId, String friendId);
}
