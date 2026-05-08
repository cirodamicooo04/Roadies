package it.roadies.travel_service.data.dao;

import it.roadies.travel_service.data.entity.FavouriteList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface FavouriteListRepository extends JpaRepository<FavouriteList, UUID> {
}
