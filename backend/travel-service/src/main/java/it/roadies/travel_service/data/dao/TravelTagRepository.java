package it.roadies.travel_service.data.dao;

import it.roadies.travel_service.data.entity.TravelTag;
import it.roadies.travel_service.data.entity.embeddables.TravelTagId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface TravelTagRepository extends JpaRepository<TravelTag, TravelTagId> {
}
