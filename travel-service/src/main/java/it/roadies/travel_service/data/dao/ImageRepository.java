package it.roadies.travel_service.data.dao;

import it.roadies.travel_service.data.entity.Image;
import it.roadies.travel_service.data.entity.enumerations.ImageStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ImageRepository extends JpaRepository<Image, UUID> {
    List<Image> findAllByStatusAndCreatedAtBefore(ImageStatus status, LocalDateTime createdAt);
}
