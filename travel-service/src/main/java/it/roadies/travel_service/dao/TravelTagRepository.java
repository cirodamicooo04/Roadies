package it.roadies.travel_service.dao;

import it.roadies.travel_service.entity.TravelTag;
import it.roadies.travel_service.entity.embeddables.TravelTagId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface TravelTagRepository extends JpaRepository<TravelTag, TravelTagId> {
}
